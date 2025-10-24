import requests
from decimal import Decimal
import os
from typing import Dict, Optional
import logging

logger = logging.getLogger(__name__)

class CurrencyService:
    def __init__(self):
        self.base_url = "https://api.exchangerate.host"
        self.cache = {}
    
    def get_exchange_rate(self, from_currency: str, to_currency: str) -> Optional[Decimal]:
        if from_currency == to_currency:
            return Decimal('1.0')
        
        cache_key = f"{from_currency}_{to_currency}"
        if cache_key in self.cache:
            return self.cache[cache_key]
        
        try:
            response = requests.get(
                f"{self.base_url}/convert",
                params={
                    "from": from_currency,
                    "to": to_currency,
                    "amount": 1
                },
                timeout=10
            )
            response.raise_for_status()
            data = response.json()
            
            if data.get('success', False):
                rate = Decimal(str(data['result'])).quantize(Decimal('0.0001'))
                self.cache[cache_key] = rate
                return rate
            else:
                logger.error(f"Currency API error: {data.get('error', 'Unknown error')}")
                return None
                
        except Exception as e:
            logger.error(f"Failed to fetch exchange rate: {e}")
            # Fallback rate if API failes
            fallback_rates = {
                "USD_EUR": Decimal('0.85'),
                "USD_GBP": Decimal('0.73'),
                "USD_RUB": Decimal('90.91'),

                "EUR_USD": Decimal('1.18'),
                "EUR_GBP": Decimal('0.86'),
                "EUR_RUB": Decimal('100.00'),

                "GBP_USD": Decimal('1.37'),
                "GBP_EUR": Decimal('1.16'),
                "GBP_RUB": Decimal('116.28'),
            
                "RUB_USD": Decimal('0.011'),
                "RUB_EUR": Decimal('0.010'),
                "RUB_GBP": Decimal('0.0086'),
            }
            return fallback_rates.get(cache_key, Decimal('1.0'))
    
    def convert_amount(self, amount: Decimal, from_currency: str, to_currency: str) -> Decimal:
        rate = self.get_exchange_rate(from_currency, to_currency)
        if rate:
            return (amount * rate).quantize(Decimal('0.01'))
        return amount

currency_service = CurrencyService()