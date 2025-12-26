import React, { useEffect, useMemo, useState } from "react";
import { apiService } from "../../services/api";
import { getCategoryIcon } from "../../utils/categoryIcons";
import "./AddTransactionModal.css";

export default function AddTransactionModal({
                                                isOpen,
                                                onClose,
                                                wallets = [],
                                                refreshData,
                                                defaultWalletId = null,
                                            }) {
    const [tab, setTab] = useState("expense"); // expense | income | transfer
    const [amount, setAmount] = useState("");
    const [walletId, setWalletId] = useState(defaultWalletId);
    const [fromWallet, setFromWallet] = useState(defaultWalletId);
    const [toWallet, setToWallet] = useState("");
    const [categoryId, setCategoryId] = useState(null);

    const [title, setTitle] = useState("");
    const [note, setNote] = useState("");
    const [attachments, setAttachments] = useState([]);

    // tags from backend (TagResponse: {id,name,...})
    const [availableTags, setAvailableTags] = useState([]);
    const [selectedTagIds, setSelectedTagIds] = useState([]);

    const [showCategoryPanel, setShowCategoryPanel] = useState(false);
    const [showTagCreator, setShowTagCreator] = useState(false);
    const [newTagName, setNewTagName] = useState("");

    // categories from backend
    const [incomeCategories, setIncomeCategories] = useState([]);
    const [expenseCategories, setExpenseCategories] = useState([]);

    // ---------- load data ----------
    useEffect(() => {
        if (!isOpen) return;

        // default wallet
        const fallbackWalletId = defaultWalletId ?? wallets?.[0]?.id ?? "";
        setWalletId((prev) => (prev ?? fallbackWalletId));
        setFromWallet((prev) => (prev ?? fallbackWalletId));

        // load categories + tags
        (async () => {
            try {
                const [income, expense, tags] = await Promise.all([
                    apiService.getIncomeCategories(),   // /api/categories/income
                    apiService.getExpenseCategories(),  // /api/categories/expense
                    apiService.getTags(),               // /api/tags/
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
        })();
    }, [isOpen, defaultWalletId, wallets]);

    // when switching tab, close the category panel and reset category if needed
    useEffect(() => {
        setShowCategoryPanel(false);

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

    const categoriesForTab = useMemo(() => {
        if (tab === "income") return incomeCategories;
        if (tab === "expense") return expenseCategories;
        return [];
    }, [tab, incomeCategories, expenseCategories]);

    if (!isOpen) return null;

    // ---------- handlers ----------
    const handleAttachment = (e) => {
        const files = Array.from(e.target.files || []);
        setAttachments((prev) => [...prev, ...files]);
    };

    const toggleTag = (tagId) => {
        setSelectedTagIds((prev) =>
            prev.includes(tagId) ? prev.filter((id) => id !== tagId) : [...prev, tagId]
        );
    };

    const createTag = async () => {
        const name = newTagName.trim();
        if (!name) return;

        try {
            const created = await apiService.createTag({ name }); // POST /api/tags/
            // created is TagResponse
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

    const submit = async () => {
        if (!amount) return;

        try {
            // TRANSFER uses dedicated endpoint
            if (tab === "transfer") {
                if (!fromWallet || !toWallet) {
                    alert("Please select both wallets.");
                    return;
                }
                await apiService.createTransfer({
                    source_wallet_id: Number(fromWallet),
                    destination_wallet_id: Number(toWallet),
                    amount: Number(amount),
                    note: note || "",
                });
            } else {
                if (!walletId) {
                    alert("Please select a wallet.");
                    return;
                }
                if (!categoryId) {
                    alert("Please select a category.");
                    return;
                }

                // Backend TransactionCreate requires: name, amount, type, transaction_date, wallet_id, category_id, tags[]
                await apiService.createTransaction({
                    name: title?.trim() || (tab === "income" ? "Income" : "Expense"),
                    amount: Number(amount),
                    note: note || "",
                    type: tab,
                    transaction_date: new Date().toISOString().split("T")[0],
                    wallet_id: Number(walletId),
                    category_id: Number(categoryId),
                    tags: selectedTagIds, // IMPORTANT: IDs (not names)
                });
            }

            refreshData?.();
            onClose?.();
        } catch (e) {
            console.error("Create transaction failed:", e);
            alert(e?.message || "Failed to create transaction");
        }
    };

    // ---------- UI helpers ----------
    const renderAmount = () => {
        const prefix = tab === "expense" ? "-" : tab === "income" ? "+" : "";
        return (
            <div className="amount-container">
                <div className="amount-display">
                    {prefix}${amount ? Number(amount).toFixed(2) : "0.00"}
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

            {/* CATEGORY (CUSTOM PANEL LIKE DESIGN) */}
            <label className="label">Category</label>
            <div className="relative">
                <div className="dropdown" onClick={() => setShowCategoryPanel((p) => !p)}>
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

            {/* ATTACHMENTS (UI ONLY) */}
            <button
                className="attachment-btn"
                onClick={() => document.getElementById("att-upload").click()}
                type="button"
            >
                Add Attachments
            </button>
            <input
                id="att-upload"
                type="file"
                className="hidden"
                multiple
                onChange={handleAttachment}
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
                        {w.name}
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
                            {w.name}
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

            <button
                className="attachment-btn"
                onClick={() => document.getElementById("att-upload").click()}
                type="button"
            >
                Add Attachments
            </button>
            <input
                id="att-upload"
                type="file"
                className="hidden"
                multiple
                onChange={handleAttachment}
            />
        </>
    );

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
                    <button type="button" className="btn-cancel" onClick={onClose}>
                        Cancel
                    </button>
                    <button type="button" className="btn-confirm" onClick={submit}>
                        Confirm Transaction
                    </button>
                </div>
            </div>

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
