import React, { useState, useEffect, useRef } from 'react';
import { apiService } from '../../services/api';
import PropTypes from 'prop-types';

const TagFilter = ({ onTagSelect, onClear, selectedTag = null, placeholder = "Filter by tag...", position = "left" }) => {
    const [tags, setTags] = useState([]);
    const [isOpen, setIsOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const dropdownRef = useRef(null);
    const inputRef = useRef(null);

    // Load tags on mount
    useEffect(() => {
        loadTags();
    }, []);

    // Close dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const loadTags = async () => {
        try {
            setIsLoading(true);
            const data = await apiService.getTags();
            setTags(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('Error loading tags:', error);
            setTags([]);
        } finally {
            setIsLoading(false);
        }
    };

    const handleSelectTag = (tag) => {
        onTagSelect(tag);
        setIsOpen(false);
        setSearchQuery('');
    };

    const handleClear = () => {
        onClear();
        setIsOpen(false);
        setSearchQuery('');
    };

    const filteredTags = tags.filter(tag =>
        tag.name.toLowerCase().includes(searchQuery.toLowerCase())
    );

    const selectedTagName = selectedTag
        ? typeof selectedTag === 'object'
            ? selectedTag.name
            : tags.find(t => t.id === selectedTag)?.name
        : null;

    return (
        <div className="relative" ref={dropdownRef}>
            <div className="flex items-center">
                {/* Filter Button */}
                <button
                    type="button"
                    onClick={() => {
                        setIsOpen(!isOpen);
                        setTimeout(() => inputRef.current?.focus(), 100);
                    }}
                    className={`flex items-center gap-2 px-3 py-2.5 rounded-xl border text-sm font-medium transition-colors ${selectedTagName
                        ? 'bg-blue-50 border-blue-200 text-blue-600'
                        : 'bg-white border-strokes text-metallic-gray hover:bg-gray-50'
                    }`}
                >
                    <svg
                        className="w-4 h-4"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="2"
                            d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
                        />
                    </svg>
                    {selectedTagName ? `#${selectedTagName}` : 'Filter'}
                    {selectedTagName && (
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                handleClear();
                            }}
                            className="ml-1 text-gray-400 hover:text-gray-600"
                        >
                            ×
                        </button>
                    )}
                </button>

                {/* Dropdown */}
                {isOpen && (
                    <div className={`absolute top-full mt-2 w-64 bg-white rounded-xl shadow-lg border border-strokes z-50 ${position === 'right' ? 'right-0' : 'left-0'
                    }`}>
                        <div className="p-3 border-b">
                            <div className="relative">
                                <div className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400">
                                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                                    </svg>
                                </div>
                                <input
                                    ref={inputRef}
                                    type="text"
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    placeholder={placeholder}
                                    className="w-full pl-10 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-200 focus:border-blue-300"
                                />
                            </div>
                        </div>

                        <div className="max-h-60 overflow-y-auto">
                            {isLoading ? (
                                <div className="py-4 text-center text-gray-500">
                                    Loading tags...
                                </div>
                            ) : filteredTags.length === 0 ? (
                                <div className="py-4 text-center text-gray-500">
                                    {searchQuery ? 'No matching tags found' : 'No tags available'}
                                </div>
                            ) : (
                                filteredTags.map(tag => (
                                    <button
                                        key={tag.id}
                                        type="button"
                                        onClick={() => handleSelectTag(tag)}
                                        className="w-full px-4 py-3 text-left hover:bg-gray-50 flex items-center justify-between"
                                    >
                                        <span className="text-text">#{tag.name}</span>
                                        {selectedTag && (
                                            (typeof selectedTag === 'object' ? selectedTag.id === tag.id : selectedTag === tag.id) && (
                                                <span className="text-blue-600">
                                                    <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                                                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                                                    </svg>
                                                </span>
                                            )
                                        )}
                                    </button>
                                ))
                            )}
                        </div>

                        <div className="p-3 border-t text-center">
                            <button
                                type="button"
                                onClick={loadTags}
                                className="text-sm text-blue-600 hover:text-blue-700"
                            >
                                Refresh tags
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

TagFilter.propTypes = {
    onTagSelect: PropTypes.func.isRequired,
    onClear: PropTypes.func.isRequired,
    selectedTag: PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.number,
        PropTypes.object
    ]),
    placeholder: PropTypes.string,
    position: PropTypes.oneOf(['left', 'right'])
};

export default TagFilter;