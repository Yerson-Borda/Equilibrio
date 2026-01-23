import React, { useEffect, useMemo, useRef, useState } from "react";
import { apiService } from "../../services/api";
import { getCategoryIcon } from "../../utils/categoryIcons";
import { getCurrencySymbol } from "../../config/currencies";
import "./AddTransactionModal.css";

export default function AddTransactionModal({
                                                isOpen,
                                                onClose,
                                                wallets = [],
                                                refreshData,
                                                defaultWalletId = null,
                                            }) {
    const [tab, setTab] = useState("expense");
    const [amount, setAmount] = useState("");
    const [walletId, setWalletId] = useState(defaultWalletId);
    const [fromWallet, setFromWallet] = useState(defaultWalletId);
    const [toWallet, setToWallet] = useState("");
    const [categoryId, setCategoryId] = useState(null);

    const [title, setTitle] = useState("");
    const [note, setNote] = useState("");
    const [attachments, setAttachments] = useState([]);

    // Attachment modal
    const [showAttachmentModal, setShowAttachmentModal] = useState(false);
    const [dragOver, setDragOver] = useState(false);
    const fileInputRef = useRef(null);

    // tags from backend
    const [availableTags, setAvailableTags] = useState([]);
    const [selectedTagIds, setSelectedTagIds] = useState([]);

    const [showCategoryPanel, setShowCategoryPanel] = useState(false);
    const [showTagCreator, setShowTagCreator] = useState(false);
    const [newTagName, setNewTagName] = useState("");

    // categories from backend
    const [incomeCategories, setIncomeCategories] = useState([]);
    const [expenseCategories, setExpenseCategories] = useState([]);

    // Exchange rate preview for transfers
    const [transferPreview, setTransferPreview] = useState(null);
    const [isLoadingPreview, setIsLoadingPreview] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const ACCEPTED_MIME =
        "image/*,application/pdf,.pdf,.png,.jpg,.jpeg,.webp,.heic,.heif,.doc,.docx,.xls,.xlsx,.csv,.txt";

    // Reset form when modal opens/closes
    useEffect(() => {
        if (!isOpen) {
            setAmount("");
            setTitle("");
            setNote("");
            setAttachments([]);
            setSelectedTagIds([]);
            setTransferPreview(null);
            setIsSubmitting(false);
            return;
        }

        // Set default wallet
        const fallbackWalletId = defaultWalletId ?? wallets?.[0]?.id ?? "";
        setWalletId(fallbackWalletId);
        setFromWallet(fallbackWalletId);
        setToWallet("");

        // Load categories and tags
        const loadData = async () => {
            try {
                const [income, expense, tags] = await Promise.all([
                    apiService.getIncomeCategories(),
                    apiService.getExpenseCategories(),
                    apiService.getTags(),
                ]);

                setIncomeCategories(Array.isArray(income) ? income : []);
                setExpenseCategories(Array.isArray(expense) ? expense : []);
                setAvailableTags(Array.isArray(tags) ? tags : []);
            } catch (e) {
                console.error("Failed to load modal data:", e);
                setIncomeCategories([]);
                setExpenseCategories([]);
                setAvailableTags([]);
            }
        };

        loadData();
    }, [isOpen, defaultWalletId, wallets]);

    // Reset when tab changes
    useEffect(() => {
        setShowCategoryPanel(false);
        setTransferPreview(null);
        setAttachments([]);

        if (tab === "income") {
            const first = incomeCategories?.[0]?.id ?? null;
            setCategoryId((prev) => prev ?? first);
        }
        if (tab === "expense") {
            const first = expenseCategories?.[0]?.id ?? null;
            setCategoryId((prev) => prev ?? first);
        }
        if (tab === "transfer") {
            setCategoryId(null);
        }
    }, [tab, incomeCategories, expenseCategories]);

    // Fetch transfer preview when transfer details change
    useEffect(() => {
        const fetchTransferPreview = async () => {
            // Reset preview if conditions aren't met
            if (tab !== "transfer" || !fromWallet || !toWallet || !amount || Number(amount) <= 0) {
                setTransferPreview(null);
                return;
            }

            if (fromWallet === toWallet) {
                setTransferPreview(null);
                return;
            }

            try {
                setIsLoadingPreview(true);
                const preview = await apiService.previewTransfer(
                    Number(fromWallet),
                    Number(toWallet),
                    Number(amount)
                );
                setTransferPreview(preview);
            } catch (error) {
                console.error("Failed to fetch transfer preview:", error);
                setTransferPreview(null);
            } finally {
                setIsLoadingPreview(false);
            }
        };

        const timer = setTimeout(fetchTransferPreview, 300);
        return () => clearTimeout(timer);
    }, [tab, fromWallet, toWallet, amount]);

    const categoriesForTab = useMemo(() => {
        if (tab === "income") return incomeCategories;
        if (tab === "expense") return expenseCategories;
        return [];
    }, [tab, incomeCategories, expenseCategories]);

    if (!isOpen) return null;

    // ---------- attachment helpers ----------
    const addFiles = (files) => {
        const incoming = Array.from(files || []);
        if (!incoming.length) return;

        setAttachments((prev) => {
            const existing = new Set(prev.map((f) => `${f.name}-${f.size}-${f.lastModified}`));
            const merged = [...prev];
            for (const f of incoming) {
                const key = `${f.name}-${f.size}-${f.lastModified}`;
                if (!existing.has(key)) merged.push(f);
            }
            return merged;
        });
    };

    const onPickFiles = (e) => {
        addFiles(e.target.files);
        e.target.value = "";
    };

    const removeAttachment = (idx) => {
        setAttachments((prev) => prev.filter((_, i) => i !== idx));
    };

    const clearAttachments = () => setAttachments([]);

    // ---------- handlers ----------
    const toggleTag = (tagId) => {
        setSelectedTagIds((prev) =>
            prev.includes(tagId) ? prev.filter((id) => id !== tagId) : [...prev, tagId]
        );
    };

    const createTag = async () => {
        const name = newTagName.trim();
        if (!name) return;

        try {
            const created = await apiService.createTag({ name });
            if (created?.id) {
                setAvailableTags((prev) => [...prev, created]);
                setSelectedTagIds((prev) => [...prev, created.id]);
            }
            setNewTagName("");
            setShowTagCreator(false);
        } catch (e) {
            console.error("Create tag failed:", e);
            alert("Failed to create tag");
        }
    };

    const extractApiError = (err) => {
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
    };

    const submit = async (e) => {
        e.preventDefault(); // Prevent default form submission

        if (isSubmitting) return; // Prevent double submission

        if (!amount || Number(amount) <= 0) {
            alert("Please enter a valid amount.");
            return;
        }

        setIsSubmitting(true);

        try {
            // TRANSFER
            if (tab === "transfer") {
                if (!fromWallet || !toWallet) {
                    alert("Please select both wallets.");
                    setIsSubmitting(false);
                    return;
                }

                if (fromWallet === toWallet) {
                    alert("Source and destination wallets must be different.");
                    setIsSubmitting(false);
                    return;
                }

                const payload = {
                    source_wallet_id: Number(fromWallet),
                    destination_wallet_id: Number(toWallet),
                    amount: Number(amount),
                    note: note || "",
                };

                console.log("Submitting transfer:", payload); // Debug log
                await apiService.createTransfer(payload);
            } else {
                // INCOME/EXPENSE
                if (!walletId) {
                    alert("Please select a wallet.");
                    setIsSubmitting(false);
                    return;
                }
                if (!categoryId) {
                    alert("Please select a category.");
                    setIsSubmitting(false);
                    return;
                }

                // Get selected tag names
                const selectedTagNames = availableTags
                    .filter(tag => selectedTagIds.includes(tag.id))
                    .map(tag => tag.name)
                    .filter(Boolean);

                const transactionData = {
                    name: title?.trim() || (tab === "income" ? "Income" : "Expense"),
                    amount: Number(amount),
                    type: tab,
                    transaction_date: new Date().toISOString().split("T")[0],
                    wallet_id: Number(walletId),
                    category_id: Number(categoryId),
                    note: note || "",
                    tags: selectedTagNames.length > 0 ? selectedTagNames.join(", ") : null,
                };

                // If there are attachments, use FormData
                if (attachments.length > 0) {
                    const formData = new FormData();

                    Object.entries(transactionData).forEach(([key, value]) => {
                        if (value !== null && value !== undefined) {
                            formData.append(key, String(value));
                        }
                    });

                    formData.append("receipt", attachments[0]);
                    await apiService.createTransaction(formData);
                } else {
                    await apiService.createTransaction(transactionData);
                }
            }

            refreshData?.();
            onClose?.();
            alert("Transaction created successfully");
        } catch (e) {
            console.error("Create transaction failed:", e);
            const errorMsg = extractApiError(e);
            alert(`Failed to create transaction: ${errorMsg}`);
        } finally {
            setIsSubmitting(false);
        }
    };

    // ---------- UI helpers ----------
    const renderAmount = () => {
        const prefix = tab === "expense" ? "-" : tab === "income" ? "+" : "";

        // Get source and target currency dynamically
        const getWalletCurrency = (id) =>
            wallets?.find((w) => String(w.id) === String(id))?.currency || "USD";

        const sourceCurrency = tab === "transfer" ? getWalletCurrency(fromWallet) : getWalletCurrency(walletId);
        const targetCurrency = tab === "transfer" ? getWalletCurrency(toWallet) : getWalletCurrency(walletId);

        const sourceSymbol = getCurrencySymbol(sourceCurrency) || sourceCurrency || "$";
        const targetSymbol = getCurrencySymbol(targetCurrency) || targetCurrency || "$";

        const fmt = (val, decimals = 2) => {
            const num = Number(val);
            if (!Number.isFinite(num)) return (0).toFixed(decimals);
            return num.toLocaleString(undefined, {
                minimumFractionDigits: decimals,
                maximumFractionDigits: decimals,
            });
        };

        const showTransferMeta =
            tab === "transfer" &&
            fromWallet &&
            toWallet &&
            String(fromWallet) !== String(toWallet) &&
            getWalletCurrency(fromWallet) !== getWalletCurrency(toWallet);

        const destCurrency =
            transferPreview?.destination_currency ||
            (showTransferMeta ? getWalletCurrency(toWallet) : "");

        return (
            <div className="amount-container">
                {/* Amount Display Box */}
                <div className="amount-display-box">
                    <div className="amount-display">
                        {prefix}
                        {sourceSymbol}
                        {fmt(amount || 0, 2)}
                    </div>

                    {/* Transfer Symbol (↔) */}
                    {tab === "transfer" && (
                        <div className="transfer-symbol">↔</div>
                    )}

                    {/* Transfer Meta Info (exchange rate, target amount) */}
                    {showTransferMeta && (
                        <div className="transfer-meta">
                            <div className="target-line">
                                Target amount -{" "}
                                <span className="target-value">
                                {isLoadingPreview || !transferPreview
                                    ? "…"
                                    : `${destCurrency} ${fmt(transferPreview.converted_amount, 2)}`}
                            </span>
                            </div>

                            {!isLoadingPreview && transferPreview?.exchange_rate && (
                                <div className="rate-line">
                                    Rate 1 {transferPreview.source_currency} ={" "}
                                    {fmt(transferPreview.exchange_rate, 6)} {transferPreview.destination_currency}
                                </div>
                            )}
                        </div>
                    )}
                </div>

                <input
                    type="number"
                    className="hidden-input"
                    onChange={(e) => setAmount(e.target.value)}
                    value={amount}
                />
            </div>
        );
    };

    const renderCategoryPanel = () => (
        <div className="category-panel">
            {categoriesForTab.map((cat) => {
                const icon = getCategoryIcon(cat.icon || cat.name);
                return (
                    <div
                        key={cat.id}
                        className="category-item"
                        onClick={() => {
                            setCategoryId(cat.id);
                            setShowCategoryPanel(false);
                        }}
                    >
                        <div
                            className="category-icon"
                            style={{ backgroundColor: cat.color || "#E5E7EB" }}
                        >
                            <img src={icon} alt={cat.name} className="icon-img" />
                        </div>
                        <span className="category-name">{cat.name}</span>
                    </div>
                );
            })}
        </div>
    );

    const renderAttachmentSummaryRow = () => (
        <button
            type="button"
            className="attachment-summary"
            onClick={() => setShowAttachmentModal(true)}
        >
            <div className="attachment-summary-left">
                <span className="attachment-summary-title">Attachments</span>
                <span className="attachment-summary-sub">
                    {attachments.length > 0 ? `${attachments.length} file(s) selected` : "Add receipt / files"}
                </span>
            </div>
            <span className="attachment-summary-action">
                {attachments.length > 0 ? "Manage" : "Add"}
            </span>
        </button>
    );

    const renderExpenseIncome = () => (
        <>
            {/* WALLET */}
            <label className="label">Wallet</label>
            <select
                className="dropdown"
                value={walletId ?? ""}
                onChange={(e) => setWalletId(Number(e.target.value))}
            >
                {wallets.map((w) => (
                    <option key={w.id} value={w.id}>
                        {w.name}
                    </option>
                ))}
            </select>

            {/* CATEGORY */}
            <label className="label">Category</label>
            <div className="relative">
                <div
                    className="dropdown"
                    onClick={() => setShowCategoryPanel(!showCategoryPanel)}
                    style={{ cursor: 'pointer' }}
                >
                    {categoryId
                        ? categoriesForTab.find((c) => c.id === categoryId)?.name
                        : "Select Category"}
                </div>
                {showCategoryPanel && renderCategoryPanel()}
            </div>

            {/* TITLE */}
            <label className="label">{tab === "expense" ? "Expense title" : "Income title"}</label>
            <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="input"
                placeholder={tab === "expense" ? "Expense title" : "Income title"}
            />

            {/* NOTE */}
            <label className="label">Note</label>
            <textarea
                className="input textarea"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder="Note"
            />

            {/* ATTACHMENTS */}
            {renderAttachmentSummaryRow()}

            {/* TAGS */}
            <div className="tags-container">
                {availableTags.map((t) => (
                    <button
                        key={t.id}
                        type="button"
                        className={`tag ${selectedTagIds.includes(t.id) ? "tag-active" : ""}`}
                        onClick={() => toggleTag(t.id)}
                    >
                        #{t.name}
                    </button>
                ))}
                <button type="button" className="tag-new" onClick={() => setShowTagCreator(true)}>
                    New Tag
                </button>
            </div>
        </>
    );

    const renderTransfer = () => (
        <>
            <label className="label">From Wallet</label>
            <select
                className="dropdown"
                value={fromWallet ?? ""}
                onChange={(e) => setFromWallet(Number(e.target.value))}
            >
                {wallets.map((w) => (
                    <option key={w.id} value={w.id}>
                        {w.name} ({w.currency})
                    </option>
                ))}
            </select>

            <label className="label">To Wallet</label>
            <select
                className="dropdown"
                value={toWallet ?? ""}
                onChange={(e) => setToWallet(Number(e.target.value))}
            >
                <option value="">Select Wallet</option>
                {wallets
                    .filter((w) => String(w.id) !== String(fromWallet))
                    .map((w) => (
                        <option key={w.id} value={w.id}>
                            {w.name} ({w.currency})
                        </option>
                    ))}
            </select>

            <label className="label">Note</label>
            <textarea
                className="input textarea"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder="Note"
            />
            {/* TAGS (REAL BACKEND) */}
            <div className="tags-container">
                {availableTags.map((t) => (
                    <button
                        key={t.id}
                        type="button"
                        className={`tag ${selectedTagIds.includes(t.id) ? "tag-active" : ""}`}
                        onClick={() => toggleTag(t.id)}
                    >
                        #{t.name}
                    </button>
                ))}
                <button type="button" className="tag-new" onClick={() => setShowTagCreator(true)}>
                    New Tag
                </button>
            </div>
        </>
    );

    const AttachmentModal = () => {
        if (!showAttachmentModal) return null;

        return (
            <div className="att-modal-overlay" onClick={() => setShowAttachmentModal(false)}>
                <div className="att-modal-box" onClick={(e) => e.stopPropagation()}>
                    <div className="att-modal-header">
                        <div className="att-modal-title">Attachments - Upload file</div>
                    </div>

                    <div
                        className={`att-dropzone ${dragOver ? "dragover" : ""}`}
                        onDragEnter={(e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            setDragOver(true);
                        }}
                        onDragOver={(e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            setDragOver(true);
                        }}
                        onDragLeave={(e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            setDragOver(false);
                        }}
                        onDrop={(e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            setDragOver(false);
                            addFiles(e.dataTransfer.files);
                        }}
                        onClick={() => fileInputRef.current?.click()}
                        role="button"
                        tabIndex={0}
                    >
                        <div className="att-drop-icon">☁️</div>
                        <div className="att-drop-title">Click or drag file to this area to upload</div>
                        <div className="att-drop-sub">Formats: images, pdf, doc/docx, xls/xlsx, csv</div>

                        <input
                            ref={fileInputRef}
                            type="file"
                            className="hidden"
                            multiple
                            accept={ACCEPTED_MIME}
                            onChange={onPickFiles}
                        />
                    </div>

                    {attachments.length > 0 && (
                        <div className="att-files">
                            {attachments.map((f, idx) => (
                                <div className="att-file-row" key={`${f.name}-${f.size}-${f.lastModified}`}>
                                    <div className="att-file-name" title={f.name}>
                                        {f.name}
                                    </div>
                                    <button
                                        type="button"
                                        className="att-file-remove"
                                        onClick={() => removeAttachment(idx)}
                                    >
                                        Remove
                                    </button>
                                </div>
                            ))}

                            <button type="button" className="att-clear" onClick={clearAttachments}>
                                Clear all
                            </button>
                        </div>
                    )}

                    <div className="att-actions">
                        <button
                            type="button"
                            className="att-cancel"
                            onClick={() => setShowAttachmentModal(false)}
                        >
                            Cancel
                        </button>
                        <button
                            type="button"
                            className="att-continue"
                            onClick={() => setShowAttachmentModal(false)}
                        >
                            Continue
                        </button>
                    </div>
                </div>
            </div>
        );
    };

    return (
        <div className="modal-overlay">
            <div className="modal-box">
                {/* TABS */}
                <div className="tabs">
                    <button
                        type="button"
                        className={`tab ${tab === "income" ? "active" : ""}`}
                        onClick={() => setTab("income")}
                    >
                        Income
                    </button>
                    <button
                        type="button"
                        className={`tab ${tab === "expense" ? "active" : ""}`}
                        onClick={() => setTab("expense")}
                    >
                        Expense
                    </button>
                    <button
                        type="button"
                        className={`tab ${tab === "transfer" ? "active" : ""}`}
                        onClick={() => setTab("transfer")}
                    >
                        Transfer
                    </button>
                </div>

                {/* AMOUNT */}
                {renderAmount()}

                <div className="form-content">
                    {tab === "transfer" ? renderTransfer() : renderExpenseIncome()}
                </div>

                {/* BUTTONS */}
                <div className="actions">
                    <button type="button" className="btn-cancel" onClick={onClose} disabled={isSubmitting}>
                        Cancel
                    </button>
                    <button type="button" className="btn-confirm" onClick={submit} disabled={isSubmitting}>
                        {isSubmitting ? "Processing..." : "Confirm Transaction"}
                    </button>
                </div>
            </div>

            {/* ATTACHMENT UPLOAD MODAL */}
            <AttachmentModal />

            {/* NEW TAG MODAL */}
            {showTagCreator && (
                <div className="tag-modal">
                    <div className="tag-box">
                        <input
                            type="text"
                            className="input"
                            placeholder="Tag title (max 8 chars)"
                            maxLength={8}
                            value={newTagName}
                            onChange={(e) => setNewTagName(e.target.value)}
                            onKeyPress={(e) => e.key === 'Enter' && createTag()}
                        />

                        <div className="tag-actions">
                            <button type="button" className="btn-confirm" onClick={createTag}>
                                Confirm
                            </button>
                            <button
                                type="button"
                                className="btn-cancel"
                                onClick={() => setShowTagCreator(false)}
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}