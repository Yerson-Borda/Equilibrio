import requests
from decimal import Decimal
from typing import Optional
import logging

logger = logging.getLogger(__name__)

class CurrencyService:
    def __init__(self):
        self.base_url = "https://api.exchangerate-api.com/v4/latest"
        self.cache = {}
    
    def get_exchange_rate(self, from_currency: str, to_currency: str) -> Optional[Decimal]:
        from_currency = from_currency.upper().strip()
        to_currency = to_currency.upper().strip()
        
        if from_currency == to_currency:
            return Decimal('1.0')
        
        cache_key = f"{from_currency}_{to_currency}"
        if cache_key in self.cache:
            return self.cache[cache_key]
        
        try:
            response = requests.get(
                f"{self.base_url}/{from_currency}",
                timeout=10
            )
            response.raise_for_status()
            data = response.json()
            
            rates = data.get('rates', {})
            if to_currency in rates:
                rate = Decimal(str(rates[to_currency])).quantize(Decimal('0.0001'))
                self.cache[cache_key] = rate
                return rate
            else:
                return self._get_fallback_rate(from_currency, to_currency)
                
        except Exception as e:
            logger.error(f"Failed to fetch exchange rate: {e}")
            return self._get_fallback_rate(from_currency, to_currency)
    
    def _get_fallback_rate(self, from_currency: str, to_currency: str) -> Decimal:
        cache_key = f"{from_currency}_{to_currency}"
        
        fallback_rates = {
            "USD_EUR": Decimal('0.92'),
            "USD_GBP": Decimal('0.79'),
            "USD_RUB": Decimal('91.50'),
            "EUR_USD": Decimal('1.09'),
            "EUR_GBP": Decimal('0.86'),
            "EUR_RUB": Decimal('99.50'),
            "GBP_USD": Decimal('1.27'),
            "GBP_EUR": Decimal('1.16'),
            "GBP_RUB": Decimal('116.00'),
            "RUB_USD": Decimal('0.0109'),
            "RUB_EUR": Decimal('0.0100'),
            "RUB_GBP": Decimal('0.0086'),
        }
        
        return fallback_rates.get(cache_key, Decimal('1.0'))
    
    def convert_amount(self, amount: Decimal, from_currency: str, to_currency: str) -> Decimal:
        rate = self.get_exchange_rate(from_currency, to_currency)
        if rate:
            return (amount * rate).quantize(Decimal('0.01'))
        return amount

currency_service = CurrencyService()