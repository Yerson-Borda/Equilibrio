import { useState, useEffect, useCallback } from 'react';
import { apiService } from '../services/api';

export const useTagFilter = (resourceType, fetchOnMount = true) => {
    const [selectedTag, setSelectedTag] = useState(null);
    const [filteredData, setFilteredData] = useState([]);
    const [allData, setAllData] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    const loadData = useCallback(async () => {
        try {
            setIsLoading(true);
            setError(null);
            let data;

            if (resourceType === 'transactions') {
                if (selectedTag) {
                    // Fetch transactions filtered by tag
                    data = await apiService.getTransactionsByTag(selectedTag.id);
                } else {
                    // Fetch all transactions
                    data = await apiService.getUserTransactions();
                }
            } else if (resourceType === 'wallets') {
                if (selectedTag) {
                    // Fetch wallets and then filter by tag on client side
                    data = await apiService.getWallets();
                    if (data && data.length > 0) {
                        // Filter wallets that have transactions with the selected tag
                        data = data.filter(wallet => {
                            if (!wallet.transactions) return false;
                            return wallet.transactions.some(transaction =>
                                transaction.tags?.some(tag => tag.id === selectedTag.id)
                            );
                        });
                    }
                } else {
                    // Fetch all wallets
                    data = await apiService.getWallets();
                }
            }

            setAllData(data || []);
            setFilteredData(data || []);

        } catch (err) {
            console.error(`Error loading ${resourceType}:`, err);
            setError(err.message);
            setAllData([]);
            setFilteredData([]);
        } finally {
            setIsLoading(false);
        }
    }, [resourceType, selectedTag]);

    // Load data when selectedTag changes or on mount
    useEffect(() => {
        if (fetchOnMount || selectedTag !== null) {
            loadData();
        }
    }, [loadData, fetchOnMount, selectedTag]);

    // Handle tag selection
    const handleTagSelect = (tag) => {
        setSelectedTag(tag);
    };

    // Clear tag filter
    const handleClearTag = () => {
        setSelectedTag(null);
    };

    // For client-side filtering without API call
    const filterDataByTag = (data, tag) => {
        if (!tag) return data;

        return data.filter(item => {
            // Handle transactions
            if (resourceType === 'transactions') {
                if (Array.isArray(item.tags)) {
                    return item.tags.some(t => t.id === tag.id || t.name === tag.name);
                }
                return item.tag?.id === tag.id || item.tag?.name === tag.name;
            }

            // Handle wallets - check if wallet has transactions with this tag
            if (resourceType === 'wallets') {
                return item.transactions?.some(transaction =>
                    transaction.tags?.some(t => t.id === tag.id || t.name === tag.name)
                );
            }

            return false;
        });
    };

    // For client-side filtering without API call
    const setDataAndFilter = (data, tag = null) => {
        setAllData(data || []);
        if (tag) {
            setSelectedTag(tag);
            const filtered = filterDataByTag(data, tag);
            setFilteredData(filtered);
        } else {
            setFilteredData(data || []);
        }
    };

    return {
        selectedTag,
        filteredData,
        allData,
        isLoading,
        error,
        handleTagSelect,
        handleClearTag,
        loadData,
        filterDataByTag,
        setDataAndFilter,
        reloadData: loadData
    };
};