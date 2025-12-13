import React, { useEffect, useMemo, useRef, useState } from 'react';
import Card from '../ui/Card';
import Button from '../ui/Button';
import CreateWalletModal from './../modals/CreateWalletModal';
import { apiService } from '../../services/api';

import visaIcon from '../../assets/icons/visa-icon.png';
import chipIcon from '../../assets/icons/chip-icon.png';
import nfcIcon from '../../assets/icons/nfc-icon.png';

import { getCurrencySymbol } from '../../config/currencies';
import { getCategoryIcon } from '../../utils/categoryIcons';

const MyWalletsContent = ({ wallets = [], onWalletCreated, onWalletDeleted }) => {
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isCreatingWallet, setIsCreatingWallet] = useState(false);
    const [selectedWallet, setSelectedWallet] = useState(wallets[0] || null);

    const [transactions, setTransactions] = useState([]);
    const [upcomingPayments, setUpcomingPayments] = useState([]);
    const [showSearch, setShowSearch] = useState(false);

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

    // Close category dropdown on outside click
    useEffect(() => {
        const onClickOutside = (e) => {
            if (!categoryDropdownRef.current) return;
            if (!categoryDropdownRef.current.contains(e.target)) setShowCategoryDropdown(false);
        };
        document.addEventListener('mousedown', onClickOutside);
        return () => document.removeEventListener('mousedown', onClickOutside);
    }, []);

    const fetchTransactions = async () => {
        try {
            const data = await apiService.getTransactions();
            setTransactions(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('Error fetching transactions:', error);
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
            if (!apiService.getTags) {
                // If your apiService doesn't have it yet, tags will still render (empty)
                setTags([]);
                return;
            }
            const data = await apiService.getTags();
            setTags(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error('Error fetching tags:', err);
            setTags([]);
        }
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
                initial_balance: parseFloat(walletData.initial_balance) || 0,
                card_number: walletData.card_number || '',
                color: walletData.color || '#6FBAFC',
            };

            const newWallet = await apiService.createWallet(formattedData);
            if (onWalletCreated) onWalletCreated(newWallet);

            setIsCreateModalOpen(false);
            setSelectedWallet(newWallet);
            alert('Wallet created successfully!');
            window.location.reload();
        } catch (error) {
            console.error('Error creating wallet:', error);
            alert(`Failed to create wallet: ${error.message || 'Please try again.'}`);
        } finally {
            setIsCreatingWallet(false);
        }
    };

    const handleDeleteWallet = async (walletId) => {
        if (!window.confirm('Are you sure you want to delete this wallet? This action cannot be undone.')) return;

        try {
            setIsCreatingWallet(true);
            await apiService.deleteWallet(walletId);
            if (onWalletDeleted) onWalletDeleted(walletId);
            if (selectedWallet?.id === walletId) setSelectedWallet(null);
            alert('Wallet deleted successfully!');
            window.location.reload();
        } catch (error) {
            console.error('Error deleting wallet:', error);
            alert('Failed to delete wallet');
        } finally {
            setIsCreatingWallet(false);
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
                alert('Please enter an amount.');
                return;
            }

            if (transactionType !== 'transfer' && !transactionForm.category_id) {
                alert('Please select a category.');
                return;
            }

            setIsSubmittingTransaction(true);

            const data = {
                ...transactionForm,
                type: transactionType,
            };

            if (transactionType === 'transfer') {
                const transferPayload = {
                    source_wallet_id: parseInt(data.source_wallet_id, 10),
                    destination_wallet_id: parseInt(data.destination_wallet_id, 10),
                    amount: parseFloat(data.amount),
                    note: data.note || '',
                    // Optional tags (if backend supports for transfers)
                    tag_ids: selectedTagIds.map((x) => (String(x).startsWith('local-') ? null : parseInt(x, 10))).filter(Boolean),
                };

                await apiService.createTransfer(transferPayload);
                alert('Transfer completed successfully!');
            } else {
                const formattedData = {
                    amount: parseFloat(data.amount),
                    name: data.title || (data.type === 'income' ? 'Income' : 'Expense'),
                    description: data.note || '',
                    type: data.type,
                    transaction_date: new Date().toISOString().split('T')[0],
                    wallet_id: parseInt(data.wallet_id || selectedWallet?.id || wallets[0]?.id, 10),
                    category_id: parseInt(data.category_id, 10),

                    // ✅ tags (send both formats so it works regardless of backend field name)
                    tag_ids: selectedTagIds
                        .map((x) => (String(x).startsWith('local-') ? null : parseInt(x, 10)))
                        .filter(Boolean),
                    tags: tags
                        .filter((t) => selectedTagIds.includes(String(t.id)))
                        .map((t) => t.name),
                };

                await apiService.createTransaction(formattedData);
                alert('Transaction added successfully!');
            }

            await fetchTransactions();
            window.dispatchEvent(new Event('transaction_updated'));
            window.location.reload();
            setIsAddTransactionOpen(false);
        } catch (error) {
            console.error('❌ Error creating transaction:', error);
            alert(`Failed to create transaction: ${error.message || 'Please try again.'}`);
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
            // If getCategoryIcon returns others for unknown, we must detect “best effort”
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

    return (
        <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* LEFT COLUMN (UNCHANGED) */}
            <div className="space-y-6" style={{ width: '450px' }}>
                <div
                    className="relative mb-20"
                    style={{
                        width: '380px',
                        height: Math.max(260, 200 + (wallets.length - 1) * 70),
                    }}
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
                        const symbol = getCurrencySymbol(wallet.currency);

                        return (
                            <div
                                key={wallet.id}
                                onClick={() => setSelectedWallet(wallet)}
                                className="absolute left-0 w-[450px] h-[250px] rounded-xl cursor-pointer transition-all duration-300"
                                style={{
                                    top: index * 70,
                                    zIndex: isSelected ? 40 : 20 - index,
                                    backgroundColor: isSelected ? wallet.color || '#6FBAFC' : 'transparent',
                                    backdropFilter: isSelected ? 'none' : 'blur(8px)',
                                    border: isSelected ? 'none' : '1px solid hsla(0, 0%, 100%, 0.30)',
                                    transform: isSelected ? 'scale(1)' : 'scale(0.97)',
                                    boxShadow: isSelected ? '0 12px 30px rgba(0,0,0,0.35)' : '0 4px 10px rgba(0,0,0,0.08)',
                                }}
                            >
                                <div className="p-6 flex flex-col justify-between h-full">
                                    <h3 className={`text-base font-semibold ${isSelected ? 'text-white' : 'text-text'}`}>
                                        {wallet.name}
                                    </h3>

                                    <div className="flex justify-between items-start mb-6">
                                        <div className="flex items-start space-x-3">
                                            <img src={chipIcon} alt="Chip" className="w-10 h-8 object-contain mt-4" />
                                            <div>
                                                <p className="text-xs opacity-75 font-bold leading-none ml-6 mt-3">Total Balance</p>
                                                <p className="text-xl font-bold leading-tight ml-6">
                                                    {symbol}
                                                    {Number(wallet.balance || 0).toFixed(2)}
                                                </p>
                                            </div>
                                        </div>
                                        <img src={nfcIcon} alt="NFC" className="w-8 h-7 object-contain mt-3" />
                                    </div>

                                    <p className={`mt-4 text-lg font-mono tracking-wider ${isSelected ? 'text-white' : 'text-text'}`}>
                                        {formatCardNumber(wallet.card_number)}
                                    </p>

                                    <div className="flex justify-between items-center mt-4">
                    <span className={`${isSelected ? 'text-white/70' : 'text-metallic-gray'} text-sm`}>
                      {wallet.expiry || '09/30'}
                    </span>

                                        <img src={visaIcon} alt="VISA" className="h-8 opacity-90" />
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
                                        {getCurrencySymbol(selectedWallet.currency)}
                                        {Number(selectedWallet.balance || 0).toFixed(2)}
                                    </p>
                                </div>

                                <button
                                    type="button"
                                    className="text-sm text-blue-500 hover:text-blue-600 flex items-center gap-1"
                                    onClick={() => console.log('Edit wallet clicked', selectedWallet)}
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
                                            onClick={() => handleDeleteWallet(selectedWallet.id)}
                                            disabled={isCreatingWallet}
                                            className="flex-[0.3] bg-red-500 text-white hover:bg-red-600 py-3 rounded-lg font-semibold"
                                        >
                                            {isCreatingWallet ? '...' : 'Delete'}
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
                                    <button className="w-full border rounded-lg p-3 bg-white text-green-600 font-semibold">
                                        Add Attachments
                                    </button>
                                </div>
                            </div>

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

                                <button onClick={() => setShowSearch(!showSearch)}>🔍</button>
                            </div>

                            {showSearch && (
                                <input type="text" placeholder="Search transactions..." className="w-full mb-4 p-2 border rounded" />
                            )}

                            <div className="space-y-3 max-h-80 overflow-y-auto">
                                {transactions.length > 0 ? (
                                    transactions.map((t, index) => {
                                        const symbol = getDisplayCurrencySymbol(t.currency);
                                        const amountSign = t.type === 'expense' ? '-' : t.type === 'income' ? '+' : '';
                                        const amountColor =
                                            t.type === 'expense' ? 'text-red-600' : t.type === 'income' ? 'text-green-600' : 'text-text';

                                        const cat = resolveTxCategory(t);

                                        return (
                                            <div key={t.id || index} className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                                                <div className="flex items-center space-x-3">
                                                    {renderTxIcon(t)}
                                                    <div>
                                                        <p className="font-bold text-text">{t.name || t.description || cat?.name || 'Transaction'}</p>
                                                        <p className="text-sm text-metallic-gray">{getTxDate(t)}</p>
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
                                    <p className="text-center text-metallic-gray py-4">No transactions yet</p>
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

            <CreateWalletModal
                isOpen={isCreateModalOpen}
                onClose={handleCloseCreateWallet}
                onSubmit={handleCreateWallet}
                isLoading={isCreatingWallet}
            />
        </div>
    );
};

export default MyWalletsContent;
