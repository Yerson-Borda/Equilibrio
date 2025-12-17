// EXPENSE CATEGORY ICONS
import foodIcon from "../assets/icons/categories/food.png";
import shoppingIcon from "../assets/icons/categories/shopping.png";
import housingIcon from "../assets/icons/categories/housing.png";
import transportIcon from "../assets/icons/categories/transport.png";
import vehicleIcon from "../assets/icons/categories/vehicle.png";
import entertainmentIcon from "../assets/icons/categories/entertainment.png";
import communicationIcon from "../assets/icons/categories/communication.png";
import investmentsIcon from "../assets/icons/categories/investments.png";

// INCOME CATEGORY ICONS
import refundsIcon from "../assets/icons/categories/refunds.png";
import rentalIncomeIcon from "../assets/icons/categories/rentalIncome.png";
import gamblingIcon from "../assets/icons/categories/gambling.png";
import lendingIcon from "../assets/icons/categories/lending.png";
import saleIcon from "../assets/icons/categories/sale.png";
import wageAndInvoicesIcon from "../assets/icons/categories/wageandinvoices.png";
import giftsIcon from "../assets/icons/categories/gifts.png";
import duesAndGrantsIcon from "../assets/icons/categories/duesandgrants.png";
import interestsIcon from "../assets/icons/categories/interests.png";

// COMMON FALLBACK
import othersIcon from "../assets/icons/categories/others.png";

// NORMALIZATION UTILITY
export function normalizeCategoryIcon(name) {
    return String(name || "")
        .toLowerCase()
        .replace(/&/g, "and")
        .replace(/\s+/g, "")
        .replace(/[^a-z0-9]/g, "");
}

// FINAL ICON MAPPING + ALIASES
export const categoryIcons = {
    // income (canonical)
    refunds: refundsIcon,
    rentalincome: rentalIncomeIcon,
    gambling: gamblingIcon,
    lending: lendingIcon,
    sale: saleIcon,
    wageandinvoices: wageAndInvoicesIcon,
    wageinvoices: wageAndInvoicesIcon,
    gifts: giftsIcon,
    duesandgrants: duesAndGrantsIcon,
    interests: interestsIcon,

    // expense (canonical)
    food: foodIcon,
    shopping: shoppingIcon,
    housing: housingIcon,
    transport: transportIcon,
    vehicle: vehicleIcon,
    entertainment: entertainmentIcon,
    communication: communicationIcon,
    investments: investmentsIcon,

    // expense aliases from your UI text (IMPORTANT)
    foodanddrinks: foodIcon,
    foodsanddrinks: foodIcon,
    drinksandfood: foodIcon,
    transportations: transportIcon,
    transportation: transportIcon,

    // income aliases from your UI text (IMPORTANT)
    rental: rentalIncomeIcon,
    rentalincomes: rentalIncomeIcon,
    dues: duesAndGrantsIcon,
    grants: duesAndGrantsIcon,

    // fallback
    others: othersIcon,
};

// Get icon safely
export function getCategoryIcon(iconName) {
    const normalized = normalizeCategoryIcon(iconName || "");
    return categoryIcons[normalized] || othersIcon;
}
