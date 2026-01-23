import React, { useEffect, useMemo, useRef, useState } from 'react';
import Card from '../ui/Card';
import Button from '../ui/Button';
import CreateWalletModal from './../modals/CreateWalletModal';
import EditWalletModal from '../modals/EditWalletModal';
import ConfirmDialog from '../ui/ConfirmDialog';
import TagFilter from '../ui/TagFilter';
import { apiService } from '../../services/api';
import { useSnackbar } from "../ui/SnackbarProvider";

import visaIcon from '../../assets/icons/visa-icon.png';
import chipIcon from '../../assets/icons/chip-icon.png';
import nfcIcon from '../../assets/icons/nfc-icon.png';

import { getCurrencySymbol, formatCurrencyMasked } from '../../config/currencies';
import { getCategoryIcon } from '../../utils/categoryIcons';

function extractApiError(err) {
    console.log('Full error:', err); // Log the full error for debugging

    const data = err?.response?.data;
    if (data) {
        if (typeof data === 'string') return data;
        if (Array.isArray(data)) {
            return data.map((x) => (typeof x === 'string' ? x : JSON.stringify(x))).join('\n');
        }
        if (typeof data === 'object') {
            if (data.detail) return String(data.detail);
            if (data.message) return String(data.message);
            if (data.error) return typeof data.error === 'string' ? data.error : JSON.stringify(data.error);
            const lines = [];
            Object.entries(data).forEach(([k, v]) => {
                if (v == null) return;
                if (Array.isArray(v)) lines.push(`${k}: ${v.join(', ')}`);
                else if (typeof v === 'object') lines.push(`${k}: ${JSON.stringify(v)}`);
                else lines.push(`${k}: ${String(v)}`);
            });
            if (lines.length) return lines.join('\n');
            return JSON.stringify(data);
        }
    }
    return err?.message ? String(err.message) : 'Unknown error';
}

const MyWalletsContent = ({
                              wallets = [],
                              onWalletCreated,
                              onWalletDeleted,
                              openTransactionOnLoad = false,
                              defaultWalletId = null,
                          }) => {
    const { showSnackbar } = useSnackbar();
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isCreatingWallet, setIsCreatingWallet] = useState(false);
    const [selectedWallet, setSelectedWallet] = useState(wallets[0] || null);
    const [hoverWalletIndex, setHoverWalletIndex] = useState(0);
    const walletStackRef = useRef(null);

    // ✅ Edit wallet
    const [isEditWalletOpen, setIsEditWalletOpen] = useState(false);
    const [walletToEdit, setWalletToEdit] = useState(null);
    const [isUpdatingWallet, setIsUpdatingWallet] = useState(false);

    // ✅ Delete wallet confirm dialog
    const [isDeleteConfirmOpen, setIsDeleteConfirmOpen] = useState(false);
    const [walletToDelete, setWalletToDelete] = useState(null);
    const [isDeletingWallet, setIsDeletingWallet] = useState(false);

    const [transactions, setTransactions] = useState([]);
    const [upcomingPayments, setUpcomingPayments] = useState([]);
    const [showSearch, setShowSearch] = useState(false);

    // Tag filter state
    const [selectedTag, setSelectedTag] = useState(null);
    const [isFilteringByTag, setIsFilteringByTag] = useState(false);

    // Inline Add Transaction
    const [isAddTransactionOpen, setIsAddTransactionOpen] = useState(false);
    const [isSubmittingTransaction, setIsSubmittingTransaction] = useState(false);
    const [transactionType, setTransactionType] = useState('income'); // income | expense | transfer

    // Categories
    const [incomeCategories, setIncomeCategories] = useState([]);
    const [expenseCategories, setExpenseCategories] = useState([]);

    // Tags
    const [tags, setTags] = useState([]);
    const [selectedTagIds, setSelectedTagIds] = useState([]);
    const [isAddingTag, setIsAddingTag] = useState(false);
    const [newTagText, setNewTagText] = useState('');

    const [transactionForm, setTransactionForm] = useState({
        amount: '',
        category_id: '',
        wallet_id: '',
        source_wallet_id: '',
        destination_wallet_id: '',
        note: '',
        title: '',
    });

    // Attachments (receipts)
    const [isAttachmentModalOpen, setIsAttachmentModalOpen] = useState(false);
    const [attachmentFiles, setAttachmentFiles] = useState([]); // File[]
    const [attachmentError, setAttachmentError] = useState('');
    const fileInputRef = useRef(null);

    const openAttachmentModal = () => {
        setAttachmentError('');
        setIsAttachmentModalOpen(true);
    };

    const closeAttachmentModal = () => {
        setAttachmentError('');
        setIsAttachmentModalOpen(false);
    };

    const addFiles = (filesLike) => {
        const list = Array.from(filesLike || []);
        if (list.length === 0) return;

        // Backend supports only a single "receipt" file per transaction
        const MAX_FILES = 1;
        const MAX_FILE_SIZE_MB = 10;

        const valid = [];
        const rejected = [];

        for (const f of list) {
            const sizeMb = f.size / (1024 * 1024);
            if (sizeMb > MAX_FILE_SIZE_MB) {
                rejected.push(`${f.name} (too large)`);
                continue;
            }
            valid.push(f);
        }

        setAttachmentFiles((prev) => {
            const merged = [...prev, ...valid];
            // remove duplicates by name+size+lastModified
            const uniq = [];
            const seen = new Set();
            for (const f of merged) {
                const key = `${f.name}|${f.size}|${f.lastModified}`;
                if (seen.has(key)) continue;
                seen.add(key);
                uniq.push(f);
            }
            return uniq.slice(0, MAX_FILES);
        });

        if (rejected.length) {
            setAttachmentError(`Some files were skipped: ${rejected.join(', ')}`);
        }
    };

    const removeAttachment = (idx) => {
        setAttachmentFiles((prev) => prev.filter((_, i) => i !== idx));
    };

    // Category dropdown
    const [showCategoryDropdown, setShowCategoryDropdown] = useState(false);
    const categoryDropdownRef = useRef(null);

    // Load initial
    useEffect(() => {
        fetchTransactions();
        fetchUpcomingPayments();
        fetchCategories();
        fetchTags();
    }, []);

    useEffect(() => {
        if (!selectedWallet && wallets.length > 0) setSelectedWallet(wallets[0]);
    }, [wallets, selectedWallet]);

    useEffect(() => {
        if (!openTransactionOnLoad) return;

        const wallet =
            wallets.find((w) => String(w.id) === String(defaultWalletId)) ||
            wallets[0] ||
            null;

        if (wallet) {
            handleAddTransactionClick(wallet);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [openTransactionOnLoad, defaultWalletId, wallets]);

    const selectedWalletIndex = useMemo(() => {
        if (!selectedWallet) return 0;
        const idx = wallets.findIndex((w) => String(w.id) === String(selectedWallet.id));
        return idx >= 0 ? idx : 0;
    }, [wallets, selectedWallet]);

    // Keep hover stack in sync with selected wallet (so mouse leave returns to selection)
    useEffect(() => {
        setHoverWalletIndex(selectedWalletIndex);
    }, [selectedWalletIndex]);

    // Close category dropdown on outside click
    useEffect(() => {
        const onClickOutside = (e) => {
            if (!categoryDropdownRef.current) return;
            if (!categoryDropdownRef.current.contains(e.target)) setShowCategoryDropdown(false);
        };
        document.addEventListener('mousedown', onClickOutside);
        return () => document.removeEventListener('mousedown', onClickOutside);
    }, []);

    const fetchTransactions = async (tagId = null) => {
        try {
            setIsFilteringByTag(!!tagId);
            let data;
            if (tagId) {
                data = await apiService.getTransactionsByTag(tagId);
            } else {
                data = await apiService.getTransactions();
            }
            setTransactions(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('Error fetching transactions:', error);
            setTransactions([]);
            showSnackbar('Failed to load transactions', { variant: 'error' });
        } finally {
            setIsFilteringByTag(false);
        }
    };

    const fetchUpcomingPayments = async () => {
        const mockUpcomingPayments = [
            { name: 'Facebook Ads', amount: 400.0, date: 'Next month' },
            { name: 'LinkedIn Ads', amount: 200.5, date: 'Next month' },
        ];
        setUpcomingPayments(mockUpcomingPayments);
    };

    const fetchCategories = async () => {
        try {
            const [income, expense] = await Promise.all([
                apiService.getIncomeCategories(),
                apiService.getExpenseCategories(),
            ]);
            setIncomeCategories(Array.isArray(income) ? income : []);
            setExpenseCategories(Array.isArray(expense) ? expense : []);
        } catch (err) {
            console.error('Error fetching categories:', err);
        }
    };

    const fetchTags = async () => {
        try {
            const data = await apiService.getTags();
            setTags(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error('Error fetching tags:', err);
            setTags([]);
        }
    };

    // Tag filter handlers
    const handleTagSelect = (tag) => {
        setSelectedTag(tag);
        fetchTransactions(tag.id);
    };

    const handleClearTag = () => {
        setSelectedTag(null);
        fetchTransactions();
    };

    const handleOpenCreateWallet = () => setIsCreateModalOpen(true);
    const handleCloseCreateWallet = () => setIsCreateModalOpen(false);

    const handleCreateWallet = async (walletData) => {
        try {
            setIsCreatingWallet(true);

            const formattedData = {
                name: walletData.name,
                currency: walletData.currency,
                wallet_type: walletData.wallet_type,
                // backend uses "balance" for create, apiService maps, but we can send balance directly too:
                balance: parseFloat(walletData.initial_balance) || 0,
                card_number: walletData.card_number || null,
                color: walletData.color || "#6FBAFC",
            };

            const newWallet = await apiService.createWallet(formattedData);

            // refresh wallets (MyWalletsPage already refetches onWalletCreated)
            if (onWalletCreated) onWalletCreated(newWallet);

            setIsCreateModalOpen(false);
            setSelectedWallet(newWallet);

            // ✅ snackbar (no reload!)
            showSnackbar("Wallet created successfully", { variant: "success" });
        } catch (error) {
            console.error("Error creating wallet:", error);
            showSnackbar(
                error?.message ? `Failed to create wallet: ${error.message}` : "Failed to create wallet",
                { variant: "error" }
            );
        } finally {
            // ✅ FIX: this was wrongly setIsDeletingWallet(false)
            setIsCreatingWallet(false);
        }
    };

    const openEditWallet = (wallet) => {
        setWalletToEdit(wallet);
        setIsEditWalletOpen(true);
    };

    const closeEditWallet = () => {
        setIsEditWalletOpen(false);
        setWalletToEdit(null);
    };

    const handleUpdateWallet = async (submitData, walletId) => {
        try {
            setIsUpdatingWallet(true);

            const updater =
                apiService.updateWallet ||
                apiService.editWallet ||
                apiService.updateWalletById;

            if (!updater) {
                throw new Error('apiService.updateWallet is not implemented');
            }

            const payload = {
                name: submitData.name,
                currency: submitData.currency,
                wallet_type: submitData.wallet_type,
                balance: Number(submitData.initial_balance) || 0,
                card_number: submitData.card_number || '',
                color: submitData.color || '#6FBAFC',
            };

            const updatedWallet = await apiService.updateWallet(walletId, payload);

            // Keep the selected wallet in sync immediately
            setSelectedWallet((prev) => {
                if (!prev) return prev;
                return String(prev.id) === String(walletId) ? { ...prev, ...updatedWallet } : prev;
            });

            showSnackbar('Wallet updated successfully', { variant: 'success' });

            // Let other parts of the app know
            window.dispatchEvent(new Event('wallet_updated'));

            closeEditWallet();
        } catch (error) {
            console.error('Error updating wallet:', error);
            showSnackbar(
                error?.message ? `Failed to update wallet: ${error.message}` : 'Failed to update wallet',
                { variant: 'error' }
            );
        } finally {
            setIsUpdatingWallet(false);
        }
    };

    const openDeleteWalletDialog = (wallet) => {
        setWalletToDelete(wallet);
        setIsDeleteConfirmOpen(true);
    };

    const closeDeleteWalletDialog = () => {
        if (isDeletingWallet) return;
        setIsDeleteConfirmOpen(false);
        setWalletToDelete(null);
    };

    const confirmDeleteWallet = async () => {
        if (!walletToDelete?.id) return;
        await handleDeleteWallet(walletToDelete.id, { skipConfirm: true });
        closeDeleteWalletDialog();
    };

    const handleDeleteWallet = async (walletId, opts = {}) => {
        const { skipConfirm = false } = opts;

        if (!skipConfirm) {
            openDeleteWalletDialog(
                wallets.find((w) => String(w.id) === String(walletId)) || selectedWallet
            );
            return;
        }

        try {
            setIsDeletingWallet(true);
            await apiService.deleteWallet(walletId);

            if (onWalletDeleted) onWalletDeleted(walletId);

            if (selectedWallet?.id === walletId) {
                setSelectedWallet(null);
            }

            // ✅ snackbar (no reload!)
            showSnackbar("Wallet deleted successfully", { variant: "success" });
        } catch (error) {
            console.error("Error deleting wallet:", error);
            showSnackbar(
                error?.message ? `Failed to delete wallet: ${error.message}` : "Failed to delete wallet",
                { variant: "error" }
            );
        } finally {
            setIsDeletingWallet(false);
        }
    };

    const handleTransactionFieldChange = (field, value) => {
        setTransactionForm((prev) => ({ ...prev, [field]: value }));
    };

    const handleAddTransactionClick = (wallet) => {
        const baseWallet = wallet || selectedWallet || wallets[0] || null;
        setSelectedWallet(baseWallet);

        setTransactionType('income');

        const defaultIncomeCategoryId = incomeCategories.length > 0 ? incomeCategories[0].id : '';

        setTransactionForm({
            amount: '',
            title: '',
            category_id: defaultIncomeCategoryId ? String(defaultIncomeCategoryId) : '',
            wallet_id: baseWallet ? String(baseWallet.id) : '',
            source_wallet_id: baseWallet ? String(baseWallet.id) : '',
            destination_wallet_id: '',
            note: '',
        });

        setSelectedTagIds([]);
        setNewTagText('');
        setIsAddingTag(false);
        setAttachmentFiles([]);

        setShowCategoryDropdown(false);
        setIsAddTransactionOpen(true);
    };

    const handleCancelTransaction = () => {
        setIsAddTransactionOpen(false);
        setIsSubmittingTransaction(false);
        setShowCategoryDropdown(false);
        setSelectedTagIds([]);
        setNewTagText('');
        setIsAddingTag(false);
        setAttachmentFiles([]);

        setTransactionForm({
            amount: '',
            title: '',
            category_id: '',
            wallet_id: '',
            source_wallet_id: '',
            destination_wallet_id: '',
            note: '',
        });
    };

    const toggleTag = (tagId) => {
        setSelectedTagIds((prev) => {
            const id = String(tagId);
            return prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id];
        });
    };

    const createTag = async () => {
        const raw = newTagText.trim();
        if (!raw) return;

        // normalize like "#work" => "work"
        const name = raw.startsWith('#') ? raw.slice(1).trim() : raw;
        if (!name) return;

        try {
            // If apiService doesn't support createTag, just create locally
            if (!apiService.createTag) {
                const fake = { id: `local-${Date.now()}`, name };
                setTags((prev) => [fake, ...prev]);
                setSelectedTagIds((prev) => [...prev, String(fake.id)]);
                setNewTagText('');
                setIsAddingTag(false);
                return;
            }

            const created = await apiService.createTag({ name });
            if (created?.id) {
                setTags((prev) => [created, ...prev]);
                setSelectedTagIds((prev) => [...prev, String(created.id)]);
            } else {
                // fallback if backend returns something unexpected
                const fake = { id: `local-${Date.now()}`, name };
                setTags((prev) => [fake, ...prev]);
                setSelectedTagIds((prev) => [...prev, String(fake.id)]);
            }

            setNewTagText('');
            setIsAddingTag(false);
        } catch (err) {
            console.error('Error creating tag:', err);
            // still allow local add (so UI doesn't block you)
            const fake = { id: `local-${Date.now()}`, name };
            setTags((prev) => [fake, ...prev]);
            setSelectedTagIds((prev) => [...prev, String(fake.id)]);
            setNewTagText('');
            setIsAddingTag(false);
        }
    };

    const handleConfirmTransaction = async () => {
        try {
            if (!transactionForm.amount) {
                showSnackbar('Please enter an amount.', { variant: 'error' });
                return;
            }

            if (transactionType !== 'transfer' && !transactionForm.category_id) {
                showSnackbar('Please select a category.', { variant: 'error' });
                return;
            }

            setIsSubmittingTransaction(true);

            const hasAttachments = attachmentFiles.length > 0;

            // TRANSFER LOGIC
            if (transactionType === 'transfer') {
                if (!transactionForm.source_wallet_id || !transactionForm.destination_wallet_id) {
                    showSnackbar('Please select both source and destination wallets.', { variant: 'error' });
                    return;
                }

                const transferPayload = {
                    source_wallet_id: parseInt(transactionForm.source_wallet_id, 10),
                    destination_wallet_id: parseInt(transactionForm.destination_wallet_id, 10),
                    amount: parseFloat(transactionForm.amount),
                    note: transactionForm.note || '',
                };

                await apiService.createTransfer(transferPayload);

            } else {
                // INCOME/EXPENSE LOGIC
                // Get selected tag IDs and convert to tag names
                const selectedTags = tags
                    .filter((t) => selectedTagIds.includes(String(t.id)))
                    .map((t) => t.name)
                    .filter(Boolean);

                // According to OpenAPI spec, tags should be sent as a comma-separated string
                const tagsString = selectedTags.length > 0 ? selectedTags.join(',') : null;

                // Prepare transaction data
                const transactionData = {
                    name: transactionForm.title || (transactionType === 'income' ? 'Income' : 'Expense'),
                    amount: parseFloat(transactionForm.amount),
                    type: transactionType,
                    transaction_date: new Date().toISOString().split('T')[0],
                    wallet_id: parseInt(transactionForm.wallet_id, 10),
                    category_id: parseInt(transactionForm.category_id, 10),
                    note: transactionForm.note || '',
                    tags: tagsString, // Send as comma-separated string
                };

                // If there are attachments, create FormData and send multipart
                if (hasAttachments) {
                    const formData = new FormData();

                    // Append all fields as strings
                    Object.entries(transactionData).forEach(([key, value]) => {
                        if (value !== null && value !== undefined) {
                            formData.append(key, String(value));
                        }
                    });

                    // Append receipt file - backend expects 'receipt' field
                    formData.append('receipt', attachmentFiles[0]);

                    await apiService.createTransaction(formData);
                } else {
                    // No attachments, send as JSON
                    await apiService.createTransaction(transactionData);
                }
            }

            await fetchTransactions(selectedTag?.id || null);
            window.dispatchEvent(new Event('transaction_updated'));
            showSnackbar('Transaction added successfully!', { variant: 'success' });
            setIsAddTransactionOpen(false);
            setAttachmentFiles([]);
            setSelectedTagIds([]);
        } catch (error) {
            console.error('❌ Error creating transaction:', error);
            console.error('Error details:', error.data);

            // Log the request data for debugging
            console.log('Transaction data sent:', {
                name: transactionForm.title || (transactionType === 'income' ? 'Income' : 'Expense'),
                amount: parseFloat(transactionForm.amount),
                type: transactionType,
                transaction_date: new Date().toISOString().split('T')[0],
                wallet_id: parseInt(transactionForm.wallet_id, 10),
                category_id: parseInt(transactionForm.category_id, 10),
                note: transactionForm.note || '',
                tags: tags.filter((t) => selectedTagIds.includes(String(t.id))).map((t) => t.name),
            });

            const msg = extractApiError(error);
            showSnackbar(`Failed to create transaction: ${msg}`, { variant: 'error' });
        } finally {
            setIsSubmittingTransaction(false);
        }
    };

    const formatCardNumber = (number) => {
        if (!number) return '•••• •••• •••• ••••';
        const digits = String(number).replace(/\D/g, '');
        const lastFour = digits.slice(-4) || '••••';
        return `•••• •••• •••• ${lastFour}`;
    };

    const getTxDate = (tx) => {
        const raw = tx.transaction_date || tx.date || tx.created_at || tx.timestamp || '';
        if (!raw) return '';
        return raw.includes('T') ? raw.split('T')[0] : raw;
    };

    const getDisplayCurrencySymbol = (currencyCode) =>
        getCurrencySymbol(currencyCode || selectedWallet?.currency || 'USD');

    const activeWalletForForm =
        wallets.find((w) => String(w.id) === String(transactionForm.wallet_id)) || selectedWallet;
    const formCurrencySymbol = getCurrencySymbol(activeWalletForForm?.currency || 'USD');

    const categoriesForCurrentType =
        transactionType === 'income'
            ? incomeCategories
            : transactionType === 'expense'
                ? expenseCategories
                : [];

    // ✅ Robust icon mapping (fixes "always others.png")
    const normalizeKey = (v) =>
        String(v || '')
            .toLowerCase()
            .trim()
            .replace(/&/g, 'and')
            .replace(/[,]/g, '')
            .replace(/[()]/g, '')
            .replace(/\s+/g, '_')
            .replace(/__+/g, '_');

    const ICON_ALIASES = useMemo(
        () => ({
            // Expense (design list)
            'food_and_drinks': 'food_drinks',
            'foods_and_drinks': 'food_drinks',
            'food_drinks': 'food_drinks',
            'shopping': 'shopping',
            'housing': 'housing',
            'transportation': 'transportation',
            'vehicle': 'vehicle',
            'entertainment': 'entertainment',
            'communication': 'communication',
            'investments': 'investments',
            'others': 'others',

            // Income (design list)
            'refunds': 'refunds',
            'rental_income': 'rental_income',
            'gambling': 'gambling',
            'lending': 'lending',
            'sale': 'sale',
            'wage_invoices': 'wage_invoices',
            'wage,_invoices': 'wage_invoices',
            'wage_invoices_': 'wage_invoices',
            'gifts': 'gifts',
            'dues_and_grants': 'dues_grants',
            'dues_grants': 'dues_grants',
            'interests': 'interests',

            // Transfers
            'transfer': 'transfers',
            'transfers': 'transfers',
        }),
        []
    );

    const getCategoryIconSafe = (cat) => {
        // Try multiple candidate keys before fallback
        const candidates = [
            cat?.icon,
            cat?.slug,
            cat?.key,
            cat?.name,
        ].filter(Boolean);

        // also try normalized versions
        const normalizedCandidates = candidates.flatMap((c) => [c, normalizeKey(c)]);

        for (const c of normalizedCandidates) {
            const key = ICON_ALIASES[normalizeKey(c)] || c;
            const icon = getCategoryIcon(key);
            // If getCategoryIcon returns others for unknown, we must detect "best effort"
            // We can't know internal mapping, so we treat alias hit as strong
            if (ICON_ALIASES[normalizeKey(c)] && icon) return icon;
        }

        // last: try normalized direct
        const finalKey = normalizeKey(cat?.name || cat?.icon || 'others');
        return getCategoryIcon(ICON_ALIASES[finalKey] || finalKey || 'others');
    };

    // Category lookup for transaction list
    const categoryById = useMemo(() => {
        const map = new Map();
        incomeCategories.forEach((c) => map.set(String(c.id), c));
        expenseCategories.forEach((c) => map.set(String(c.id), c));
        return map;
    }, [incomeCategories, expenseCategories]);

    const resolveTxCategory = (tx) => {
        if (tx?.category && (tx.category.name || tx.category.icon)) return tx.category;
        const id = tx?.category_id ?? tx?.categoryId;
        if (!id) return null;
        return categoryById.get(String(id)) || null;
    };

    const renderTxIcon = (tx) => {
        if (tx?.type === 'transfer') {
            return (
                <div className="w-10 h-10 rounded-full bg-[#E9EEF5] flex items-center justify-center">
                    <img src={getCategoryIcon('transfers')} alt="transfer" className="w-5 h-5" />
                </div>
            );
        }

        const cat = resolveTxCategory(tx);
        const bg = cat?.color || '#E9EEF5';
        return (
            <div className="w-10 h-10 rounded-full flex items-center justify-center" style={{ backgroundColor: bg }}>
                <img src={getCategoryIconSafe(cat)} alt={cat?.name || 'category'} className="w-5 h-5" />
            </div>
        );
    };

    const selectedCategory = useMemo(() => {
        if (!transactionForm.category_id) return null;
        return (
            categoriesForCurrentType.find((c) => String(c.id) === String(transactionForm.category_id)) || null
        );
    }, [transactionForm.category_id, categoriesForCurrentType]);

    // Filter transactions by selected tag
    const filteredTransactions = useMemo(() => {
        // If we're filtering by tag via API, use the already filtered transactions
        if (selectedTag) {
            return transactions.filter((t) => {
                // Check if transaction has tags array and includes the selected tag
                if (Array.isArray(t.tags)) {
                    return t.tags.some(tag =>
                        tag.id === selectedTag.id ||
                        tag.name === selectedTag.name ||
                        String(tag) === String(selectedTag.id) ||
                        String(tag) === selectedTag.name
                    );
                }

                // If tags is a string, check if it contains the tag name
                if (typeof t.tags === 'string') {
                    return t.tags.toLowerCase().includes(selectedTag.name.toLowerCase());
                }

                return false;
            });
        }

        return transactions;
    }, [transactions, selectedTag]);

    // Function to get tags from a transaction
    const getTransactionTags = (transaction) => {
        if (!transaction.tags) return [];

        if (Array.isArray(transaction.tags)) {
            return transaction.tags;
        }

        if (typeof transaction.tags === 'string') {
            // Handle comma-separated string of tag names
            return transaction.tags.split(',').map(tagName => ({
                id: `string-${tagName.trim()}`,
                name: tagName.trim()
            }));
        }

        return [];
    };

    return (
        <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* LEFT COLUMN (UNCHANGED) */}
            <div className="space-y-6" style={{ width: '450px' }}>
                <div
                    ref={walletStackRef}
                    className="relative mb-20 overflow-hidden select-none"
                    style={{
                        width: '450px',
                        height: 260, // fixed so the page never grows with N wallets
                    }}
                    onMouseMove={(e) => {
                        if (!walletStackRef.current) return;
                        const rect = walletStackRef.current.getBoundingClientRect();
                        const y = e.clientY - rect.top;

                        const peek = 32;
                        const idx = Math.max(0, Math.min(wallets.length - 1, Math.floor(y / peek)));
                        setHoverWalletIndex(idx);
                    }}
                    onMouseLeave={() => setHoverWalletIndex(selectedWalletIndex)}
                >
                    {wallets.length === 0 && (
                        <Card className="p-6 text-center">
                            <p className="font-medium text-text">No wallets yet</p>
                            <p className="text-sm text-metallic-gray mb-3">Create a wallet to get started.</p>
                            <Button variant="primary" onClick={handleOpenCreateWallet}>
                                + Add Wallet
                            </Button>
                        </Card>
                    )}

                    {wallets.map((wallet, index) => {
                        const isSelected = selectedWallet?.id === wallet.id;
                        const isActive = index === hoverWalletIndex;
                        const symbol = getCurrencySymbol(wallet.currency);

                        const peek = 32;
                        const maxAbove = peek * 2;
                        const top = Math.max(-maxAbove, (index - hoverWalletIndex) * peek);

                        const distance = Math.abs(index - hoverWalletIndex);
                        const scale = 1 - Math.min(0.03 * distance, 0.12);

                        return (
                            <div
                                key={wallet.id}
                                onClick={() => {
                                    setSelectedWallet(wallet);
                                    setHoverWalletIndex(index);
                                }}
                                className="absolute left-0 w-[450px] h-[250px] rounded-xl cursor-pointer transition-all duration-300"
                                style={{
                                    top,
                                    zIndex: 50 - distance,
                                    backgroundColor: isActive
                                        ? wallet.color || '#6FBAFC'
                                        : 'rgba(255,255,255,0.72)',
                                    backdropFilter: isActive ? 'none' : 'blur(8px)',
                                    border: isActive ? 'none' : '1px solid hsla(0, 0%, 100%, 0.30)',
                                    transform: `scale(${scale})`,
                                    boxShadow: isActive
                                        ? '0 12px 30px rgba(0,0,0,0.35)'
                                        : '0 4px 10px rgba(0,0,0,0.08)',
                                }}
                            >
                                <div className="p-6 flex flex-col justify-between h-full">
                                    <h3 className={`text-base font-semibold ${isActive ? 'text-white' : 'text-text'}`}>
                                        {wallet.name}
                                    </h3>

                                    <div className="flex justify-between items-start mb-6">
                                        <div className="flex items-start space-x-3">
                                            <img src={chipIcon} alt="Chip" className="w-10 h-8 object-contain mt-4" />
                                            <div>
                                                <p className={`text-xs opacity-75 font-bold leading-none ml-6 mt-3 ${isActive ? 'text-white/80' : 'text-metallic-gray'}`}>
                                                    Total Balance
                                                </p>
                                                <p className={`text-xl font-bold leading-tight ml-6 ${isActive ? 'text-white' : 'text-text'}`}>
                                                    {formatCurrencyMasked(wallet.balance || 0, symbol, 15)}
                                                </p>
                                            </div>
                                        </div>
                                        <img src={nfcIcon} alt="NFC" className="w-8 h-7 object-contain mt-3" />
                                    </div>

                                    <p className={`mt-4 text-lg font-mono tracking-wider ${isActive ? 'text-white' : 'text-text'}`}>
                                        {formatCardNumber(wallet.card_number)}
                                    </p>

                                    <div className="flex justify-between items-center mt-4">
                                        <span className={`${isActive ? 'text-white/70' : 'text-metallic-gray'} text-sm`}>
                                            {wallet.expiry || '09/30'}
                                        </span>

                                        <img src={visaIcon} alt="VISA" className={`h-8 ${isActive ? 'opacity-90' : 'opacity-70'}`} />
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>

                {selectedWallet && (
                    <Card>
                        <div className="space-y-4">
                            <div className="flex items-start justify-between">
                                <div>
                                    <p className="text-sm text-metallic-gray">Balance</p>
                                    <p className="text-2xl font-bold text-text">
                                        {formatCurrencyMasked(
                                            selectedWallet.balance || 0,
                                            getCurrencySymbol(selectedWallet.currency),
                                            16
                                        )}
                                    </p>
                                </div>

                                <button
                                    type="button"
                                    className="text-sm text-blue-500 hover:text-blue-600 flex items-center gap-1"
                                    onClick={() => openEditWallet(selectedWallet)}
                                >
                                    ✏️ <span className="hidden sm:inline">Edit</span>
                                </button>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <p className="text-sm text-metallic-gray">Currency</p>
                                    <p className="font-medium">{selectedWallet.currency}</p>
                                </div>

                                <div>
                                    <p className="text-sm text-metallic-gray">Type</p>
                                    <p className="font-medium capitalize">
                                        {selectedWallet.wallet_type?.replace('_', ' ') || 'debit'}
                                    </p>
                                </div>
                            </div>

                            <div className="space-y-3 pt-2">
                                <button
                                    onClick={handleOpenCreateWallet}
                                    className="w-full bg-white border-2 border-green-500 text-green-500 hover:bg-green-50 py-3 rounded-lg font-semibold"
                                >
                                    + Add Wallet
                                </button>

                                {!isAddTransactionOpen && (
                                    <div className="flex space-x-3">
                                        <button
                                            onClick={() => handleAddTransactionClick(selectedWallet)}
                                            className="flex-[0.7] bg-white border-2 border-green-500 text-green-500 hover:bg-green-50 py-3 rounded-lg font-semibold"
                                        >
                                            Add Transaction
                                        </button>

                                        <button
                                            onClick={() => openDeleteWalletDialog(selectedWallet)}
                                            disabled={isDeletingWallet}
                                            className="flex-[0.3] bg-red-500 text-white hover:bg-red-600 py-3 rounded-lg font-semibold"
                                        >
                                            {isDeletingWallet ? '...' : 'Delete'}
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>
                    </Card>
                )}
            </div>

            {/* RIGHT COLUMN */}
            <div className="space-y-8" style={{ width: '800px' }}>
                {isAddTransactionOpen ? (
                    <Card>
                        <div className="p-2">
                            <h2 className="text-xl font-semibold mb-4">Add Transaction</h2>

                            {/* Tabs */}
                            <div className="flex space-x-6 border-b mb-6">
                                {['income', 'expense', 'transfer'].map((t) => (
                                    <button
                                        key={t}
                                        onClick={() => {
                                            setTransactionType(t);
                                            setShowCategoryDropdown(false);

                                            // default category on tab switch
                                            if (t === 'income' && incomeCategories.length > 0) {
                                                setTransactionForm((p) => ({
                                                    ...p,
                                                    category_id: p.category_id || String(incomeCategories[0].id),
                                                }));
                                            }
                                            if (t === 'expense' && expenseCategories.length > 0) {
                                                setTransactionForm((p) => ({
                                                    ...p,
                                                    category_id: p.category_id || String(expenseCategories[0].id),
                                                }));
                                            }
                                            if (t === 'transfer') {
                                                setTransactionForm((p) => ({ ...p, category_id: '' }));
                                            }
                                        }}
                                        className={`pb-2 text-sm capitalize ${
                                            transactionType === t ? 'text-blue font-semibold border-b-2 border-blue' : 'text-metallic-gray'
                                        }`}
                                    >
                                        {t}
                                    </button>
                                ))}
                            </div>

                            {/* Amount header */}
                            <div className="flex items-start justify-between bg-white rounded-xl border p-6 mb-6">
                                <div className="text-5xl font-bold text-text">
                                    {transactionType === 'income' ? '+' : transactionType === 'expense' ? '-' : '↔'}
                                </div>

                                <div className="text-right">
                                    <div className="text-5xl font-extrabold text-text">
                                        {formCurrencySymbol}
                                        {Number(transactionForm.amount || 0).toFixed(2)}
                                    </div>

                                    <input
                                        type="number"
                                        step="0.01"
                                        min="0"
                                        className="mt-3 w-56 border rounded-lg p-2"
                                        placeholder="0.00"
                                        value={transactionForm.amount}
                                        onChange={(e) => handleTransactionFieldChange('amount', e.target.value)}
                                    />
                                </div>
                            </div>

                            {/* Wallet + Category / Transfer wallets */}
                            {transactionType === 'transfer' ? (
                                <div className="grid grid-cols-2 gap-4 mb-6">
                                    <div>
                                        <p className="text-xs text-metallic-gray mb-1">From Wallet</p>
                                        <select
                                            className="border rounded-lg p-2 w-full"
                                            value={transactionForm.source_wallet_id}
                                            onChange={(e) => handleTransactionFieldChange('source_wallet_id', e.target.value)}
                                        >
                                            <option value="">Select wallet</option>
                                            {wallets.map((w) => (
                                                <option key={w.id} value={w.id}>
                                                    {w.name}
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    <div>
                                        <p className="text-xs text-metallic-gray mb-1">To Wallet</p>
                                        <select
                                            className="border rounded-lg p-2 w-full"
                                            value={transactionForm.destination_wallet_id}
                                            onChange={(e) => handleTransactionFieldChange('destination_wallet_id', e.target.value)}
                                        >
                                            <option value="">Select wallet</option>
                                            {wallets.map((w) => (
                                                <option key={w.id} value={w.id}>
                                                    {w.name}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                </div>
                            ) : (
                                <div className="grid grid-cols-2 gap-4 mb-6">
                                    <div>
                                        <p className="text-xs text-metallic-gray mb-1">Wallet</p>
                                        <select
                                            className="border rounded-lg p-2 w-full"
                                            value={transactionForm.wallet_id}
                                            onChange={(e) => handleTransactionFieldChange('wallet_id', e.target.value)}
                                        >
                                            {wallets.map((w) => (
                                                <option key={w.id} value={w.id}>
                                                    {w.name}
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    {/* CATEGORY DROPDOWN WITH ICONS (FIXED) */}
                                    <div ref={categoryDropdownRef} className="relative">
                                        <p className="text-xs text-metallic-gray mb-1">Category</p>

                                        <button
                                            type="button"
                                            onClick={() => setShowCategoryDropdown((v) => !v)}
                                            className="w-full border rounded-lg p-3 flex items-center justify-between bg-white"
                                        >
                                            {selectedCategory ? (
                                                <span className="flex items-center gap-3">
                                                    <span
                                                        className="w-8 h-8 rounded-full flex items-center justify-center"
                                                        style={{ backgroundColor: selectedCategory.color || '#E9EEF5' }}
                                                    >
                                                        <img
                                                            src={getCategoryIconSafe(selectedCategory)}
                                                            alt={selectedCategory.name}
                                                            className="w-4 h-4"
                                                        />
                                                    </span>
                                                    <span className="font-medium">{selectedCategory.name}</span>
                                                </span>
                                            ) : (
                                                <span className="text-metallic-gray">Select category</span>
                                            )}

                                            <span className="text-metallic-gray">▾</span>
                                        </button>

                                        {showCategoryDropdown && (
                                            <div className="absolute z-50 mt-2 w-full bg-white rounded-xl shadow-lg border max-h-72 overflow-y-auto">
                                                {categoriesForCurrentType.map((cat) => (
                                                    <button
                                                        key={cat.id}
                                                        type="button"
                                                        onClick={() => {
                                                            handleTransactionFieldChange('category_id', String(cat.id));
                                                            setShowCategoryDropdown(false);
                                                        }}
                                                        className="w-full px-4 py-3 flex items-center gap-4 hover:bg-gray-50 text-left"
                                                    >
                                                        <span
                                                            className="w-10 h-10 rounded-full flex items-center justify-center"
                                                            style={{ backgroundColor: cat.color || '#E9EEF5' }}
                                                        >
                                                            <img src={getCategoryIconSafe(cat)} alt={cat.name} className="w-5 h-5" />
                                                        </span>

                                                        <span className="font-medium text-text">{cat.name}</span>
                                                    </button>
                                                ))}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            )}

                            {/* Title + Attachments */}
                            {transactionType === 'transfer' ? (
                                <div className="mb-4">
                                    <button
                                        type="button"
                                        onClick={openAttachmentModal}
                                        className="w-full border rounded-lg p-3 bg-white text-green-600 font-semibold hover:bg-green-50"
                                    >
                                        Add Attachments{attachmentFiles.length ? ` (${attachmentFiles.length})` : ''}
                                    </button>
                                </div>
                            ) : (
                                <div className="grid grid-cols-12 gap-4 mb-4">
                                    <div className="col-span-12 md:col-span-7">
                                        <input
                                            type="text"
                                            className="w-full border rounded-lg p-3"
                                            placeholder={transactionType === 'income' ? 'Income title' : 'Expense title'}
                                            value={transactionForm.title}
                                            onChange={(e) => handleTransactionFieldChange('title', e.target.value)}
                                        />
                                    </div>
                                    <div className="col-span-12 md:col-span-5">
                                        <button
                                            type="button"
                                            onClick={openAttachmentModal}
                                            className="w-full border rounded-lg p-3 bg-white text-green-600 font-semibold hover:bg-green-50"
                                        >
                                            Add Attachments{attachmentFiles.length ? ` (${attachmentFiles.length})` : ''}
                                        </button>
                                    </div>
                                </div>
                            )}

                            {/* Selected attachments preview */}
                            {attachmentFiles.length > 0 && (
                                <div className="mb-4">
                                    <p className="text-xs text-metallic-gray mb-2">Attachments</p>
                                    <div className="flex flex-wrap gap-2">
                                        {attachmentFiles.map((f, idx) => (
                                            <span
                                                key={`${f.name}-${f.size}-${f.lastModified}-${idx}`}
                                                className="inline-flex items-center gap-2 px-3 py-1 rounded-full border bg-white text-sm"
                                            >
                                                <span className="truncate max-w-[240px]">{f.name}</span>
                                                <button
                                                    type="button"
                                                    onClick={() => removeAttachment(idx)}
                                                    className="text-metallic-gray hover:text-red-500"
                                                    aria-label="Remove attachment"
                                                >
                                                    ✕
                                                </button>
                                            </span>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* NOTE */}
                            <div className="mb-4">
                                <textarea
                                    className="w-full border rounded-lg p-3 h-28 resize-none"
                                    placeholder="Note"
                                    value={transactionForm.note}
                                    onChange={(e) => handleTransactionFieldChange('note', e.target.value)}
                                />
                            </div>

                            {/* TAGS (added back like design) */}
                            <div className="mb-6">
                                <div className="flex flex-wrap gap-3 items-center">
                                    {tags.slice(0, 20).map((t) => {
                                        const active = selectedTagIds.includes(String(t.id));
                                        return (
                                            <button
                                                key={t.id}
                                                type="button"
                                                onClick={() => toggleTag(t.id)}
                                                className={`px-5 py-2 rounded-xl border text-sm font-semibold ${
                                                    active
                                                        ? 'bg-[#6FBAFC] border-[#6FBAFC] text-white'
                                                        : 'bg-white border-gray-200 text-text hover:bg-gray-50'
                                                }`}
                                            >
                                                #{t.name}
                                            </button>
                                        );
                                    })}

                                    {!isAddingTag ? (
                                        <button
                                            type="button"
                                            onClick={() => setIsAddingTag(true)}
                                            className="px-5 py-2 rounded-xl border text-sm font-semibold bg-white border-gray-200 text-text hover:bg-gray-50"
                                        >
                                            New Tag
                                        </button>
                                    ) : (
                                        <div className="flex items-center gap-2">
                                            <input
                                                value={newTagText}
                                                onChange={(e) => setNewTagText(e.target.value)}
                                                onKeyDown={(e) => {
                                                    if (e.key === 'Enter') createTag();
                                                    if (e.key === 'Escape') {
                                                        setIsAddingTag(false);
                                                        setNewTagText('');
                                                    }
                                                }}
                                                className="px-4 py-2 rounded-xl border border-gray-200 text-sm w-44"
                                                placeholder="#tag"
                                            />
                                            <button
                                                type="button"
                                                onClick={createTag}
                                                className="px-4 py-2 rounded-xl bg-[#6FBAFC] text-white text-sm font-semibold"
                                            >
                                                Add
                                            </button>
                                            <button
                                                type="button"
                                                onClick={() => {
                                                    setIsAddingTag(false);
                                                    setNewTagText('');
                                                }}
                                                className="px-4 py-2 rounded-xl bg-white border border-gray-200 text-sm font-semibold"
                                            >
                                                Cancel
                                            </button>
                                        </div>
                                    )}
                                </div>
                                <p className="text-xs text-metallic-gray mt-2">
                                    Selected tags: {selectedTagIds.length > 0 ? selectedTagIds.map(id => {
                                    const tag = tags.find(t => String(t.id) === id);
                                    return tag ? `#${tag.name}` : '';
                                }).filter(Boolean).join(', ') : 'None'}
                                </p>
                            </div>

                            {/* Buttons */}
                            <div className="flex space-x-4">
                                <button
                                    onClick={handleConfirmTransaction}
                                    disabled={isSubmittingTransaction}
                                    className="flex-1 bg-[#16A34A]/10 text-[#16A34A] font-semibold py-3 rounded-xl hover:bg-[#16A34A]/20 disabled:opacity-60"
                                >
                                    {isSubmittingTransaction ? 'Saving...' : 'Confirm Transaction'}
                                </button>

                                <button
                                    onClick={handleCancelTransaction}
                                    className="flex-1 bg-red-500 text-white font-semibold py-3 rounded-xl hover:bg-red-600"
                                >
                                    Cancel
                                </button>
                            </div>
                        </div>
                    </Card>
                ) : (
                    <>
                        <Card title="Transactions">
                            <div className="flex justify-between items-center mb-4">
                                <div className="flex space-x-4 items-center">
                                    <button className="text-blue font-medium border-b-2 border-blue pb-1 text-sm">
                                        All Transactions
                                    </button>
                                    <button className="text-metallic-gray pb-1 text-sm">Regular Transactions</button>
                                </div>

                                <div className="flex items-center gap-2">
                                    <button onClick={() => setShowSearch(!showSearch)}>🔍</button>
                                    <TagFilter
                                        onTagSelect={handleTagSelect}
                                        onClear={handleClearTag}
                                        selectedTag={selectedTag}
                                        placeholder="Filter by tag..."
                                        position="right"
                                    />
                                </div>
                            </div>

                            {showSearch && (
                                <input type="text" placeholder="Search transactions..." className="w-full mb-4 p-2 border rounded" />
                            )}

                            {/* Selected tag indicator */}
                            {selectedTag && (
                                <div className="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center gap-2">
                                            <span className="text-sm text-gray-600">Filtered by:</span>
                                            <span className="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-sm font-medium">
                                                #{selectedTag.name}
                                            </span>
                                            <span className="text-sm text-gray-500">
                                                ({filteredTransactions.length} transactions found)
                                            </span>
                                        </div>
                                        <button
                                            onClick={handleClearTag}
                                            className="text-sm text-blue-600 hover:text-blue-800 font-medium"
                                        >
                                            Clear filter
                                        </button>
                                    </div>
                                </div>
                            )}

                            {/* Loading state for tag filtering */}
                            {isFilteringByTag && (
                                <div className="flex justify-center py-4">
                                    <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
                                </div>
                            )}

                            <div className="space-y-3 max-h-80 overflow-y-auto">
                                {filteredTransactions.length > 0 ? (
                                    filteredTransactions.map((t, index) => {
                                        const symbol = getDisplayCurrencySymbol(t.currency);
                                        const amountSign = t.type === 'expense' ? '-' : t.type === 'income' ? '+' : '';
                                        const amountColor =
                                            t.type === 'expense' ? 'text-red-600' : t.type === 'income' ? 'text-green-600' : 'text-text';

                                        const cat = resolveTxCategory(t);
                                        const transactionTags = getTransactionTags(t);

                                        return (
                                            <div key={t.id || index} className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                                                <div className="flex items-center space-x-3">
                                                    {renderTxIcon(t)}
                                                    <div className="flex-1">
                                                        <p className="font-bold text-text">{t.name || t.description || cat?.name || 'Transaction'}</p>
                                                        <p className="text-sm text-metallic-gray">{getTxDate(t)}</p>

                                                        {/* Show transaction tags if available */}
                                                        {transactionTags.length > 0 && (
                                                            <div className="flex flex-wrap gap-1 mt-1">
                                                                {transactionTags.slice(0, 3).map((tag, idx) => (
                                                                    <span
                                                                        key={idx}
                                                                        className="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium bg-blue-100 text-blue-800"
                                                                    >
                                                                        #{typeof tag === 'object' ? tag.name : tag}
                                                                    </span>
                                                                ))}
                                                                {transactionTags.length > 3 && (
                                                                    <span className="text-xs text-gray-500">
                                                                        +{transactionTags.length - 3} more
                                                                    </span>
                                                                )}
                                                            </div>
                                                        )}
                                                    </div>
                                                </div>

                                                <p className={`font-semibold ${amountColor}`}>
                                                    {amountSign}
                                                    {symbol}
                                                    {Number(t.amount || 0).toFixed(2)}
                                                </p>
                                            </div>
                                        );
                                    })
                                ) : (
                                    <div className="text-center py-8">
                                        <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                        </svg>
                                        <h3 className="mt-2 text-sm font-medium text-gray-900">No transactions found</h3>
                                        <p className="mt-1 text-sm text-gray-500">
                                            {selectedTag
                                                ? `No transactions found with tag #${selectedTag.name}`
                                                : 'Get started by creating your first transaction.'}
                                        </p>
                                        {selectedTag && (
                                            <button
                                                onClick={handleClearTag}
                                                className="mt-4 text-sm text-blue-600 hover:text-blue-800 font-medium"
                                            >
                                                Clear tag filter
                                            </button>
                                        )}
                                    </div>
                                )}
                            </div>
                        </Card>

                        <Card title="Upcoming Payments" className="bg-[#F5F7FA] border-none">
                            {upcomingPayments.length > 0 ? (
                                upcomingPayments.map((p, index) => (
                                    <div key={index} className="flex justify-between items-center p-3 bg-gray-50 rounded-lg mb-3">
                                        <div className="flex items-center space-x-3">
                                            <div className="w-10 h-10 rounded-full bg-[#E9EEF5] flex items-center justify-center">
                                                <img src={getCategoryIcon('others')} alt="upcoming" className="w-5 h-5" />
                                            </div>
                                            <div>
                                                <p className="font-bold text-text">{p.name}</p>
                                                <p className="text-sm text-metallic-gray">{p.date}</p>
                                            </div>
                                        </div>

                                        <p className="font-semibold text-text">
                                            {getDisplayCurrencySymbol(p.currency || selectedWallet?.currency)}
                                            {Number(p.amount || 0).toFixed(2)}
                                        </p>
                                    </div>
                                ))
                            ) : (
                                <p className="text-center text-metallic-gray py-4">No upcoming payments</p>
                            )}
                        </Card>
                    </>
                )}
            </div>

            {/* Attachments modal (like design) */}
            {isAttachmentModalOpen && (
                <div
                    className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/40 px-4"
                    onMouseDown={(e) => {
                        // close when clicking backdrop
                        if (e.target === e.currentTarget) closeAttachmentModal();
                    }}
                >
                    <div className="w-full max-w-lg bg-white rounded-2xl shadow-xl overflow-hidden">
                        <div className="px-6 py-4 border-b">
                            <p className="text-sm font-semibold text-text">Attachments - Upload file</p>
                        </div>

                        <div className="p-6">
                            <div className="text-sm font-semibold text-text mb-3">File Upload</div>

                            <div
                                className="rounded-xl border border-dashed border-gray-300 bg-[#FBFBFD] p-8 text-center cursor-pointer select-none"
                                onClick={() => fileInputRef.current?.click()}
                                onDragOver={(e) => {
                                    e.preventDefault();
                                    e.stopPropagation();
                                }}
                                onDrop={(e) => {
                                    e.preventDefault();
                                    e.stopPropagation();
                                    addFiles(e.dataTransfer.files);
                                }}
                            >
                                <div className="mx-auto mb-3 w-10 h-10 rounded-full bg-white border flex items-center justify-center">
                                    <span className="text-xl">☁️</span>
                                </div>
                                <p className="text-sm text-text">
                                    Click or drag file to this area to upload
                                </p>
                                <p className="text-xs text-metallic-gray mt-2">
                                    Formats accepted are .png, .jpg, .jpeg, .pdf, .doc, .docx
                                </p>

                                <input
                                    ref={fileInputRef}
                                    type="file"
                                    className="hidden"
                                    multiple
                                    accept=".png,.jpg,.jpeg,.pdf,.doc,.docx"
                                    onChange={(e) => {
                                        addFiles(e.target.files);
                                        // reset input so same file can be added again if needed
                                        e.target.value = '';
                                    }}
                                />
                            </div>

                            {attachmentError && (
                                <p className="text-xs text-red-500 mt-3">{attachmentError}</p>
                            )}

                            {attachmentFiles.length > 0 && (
                                <div className="mt-5">
                                    <p className="text-xs text-metallic-gray mb-2">
                                        Selected file(s)
                                    </p>
                                    <div className="space-y-2">
                                        {attachmentFiles.map((f, idx) => (
                                            <div
                                                key={`${f.name}-${f.size}-${f.lastModified}-${idx}`}
                                                className="flex items-center justify-between px-4 py-3 rounded-xl border bg-white"
                                            >
                                                <div className="flex items-center gap-3 min-w-0">
                                                    <div className="w-9 h-9 rounded-lg bg-[#E9EEF5] flex items-center justify-center">
                                                        📄
                                                    </div>
                                                    <div className="min-w-0">
                                                        <p className="text-sm font-medium text-text truncate">{f.name}</p>
                                                        <p className="text-xs text-metallic-gray">
                                                            {(f.size / 1024).toFixed(0)} KB
                                                        </p>
                                                    </div>
                                                </div>

                                                <button
                                                    type="button"
                                                    onClick={() => removeAttachment(idx)}
                                                    className="text-sm text-red-500 hover:text-red-600 font-semibold"
                                                >
                                                    Remove
                                                </button>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            <div className="mt-6 flex justify-end gap-3">
                                <button
                                    type="button"
                                    onClick={closeAttachmentModal}
                                    className="px-5 py-2.5 rounded-lg border bg-white font-semibold"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="button"
                                    onClick={closeAttachmentModal}
                                    className="px-5 py-2.5 rounded-lg bg-[#3B82F6] text-white font-semibold"
                                >
                                    Continue
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            <CreateWalletModal
                isOpen={isCreateModalOpen}
                onClose={handleCloseCreateWallet}
                onSubmit={handleCreateWallet}
                isLoading={isCreatingWallet}
            />

            <EditWalletModal
                isOpen={isEditWalletOpen}
                onClose={closeEditWallet}
                onSubmit={(submitData, walletId) => handleUpdateWallet(submitData, walletId)}
                isLoading={isUpdatingWallet}
                wallet={walletToEdit}
            />

            <ConfirmDialog
                isOpen={isDeleteConfirmOpen}
                title={`Do you really want to delete ${walletToDelete?.name || ''} wallet?`}
                message="The wallet will be deleted with all the records and related objects."
                cancelText="Cancel"
                confirmText="Delete"
                danger
                loading={isDeletingWallet}
                onCancel={closeDeleteWalletDialog}
                onConfirm={confirmDeleteWallet}
            />
        </div>
    );
};

export default MyWalletsContent;