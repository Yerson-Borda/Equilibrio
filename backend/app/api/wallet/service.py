from sqlalchemy.orm import Session
from fastapi import HTTPException, status
from decimal import Decimal
from app.entities.wallet import Wallet
from app.entities.transaction import Transaction
from app.services.currency_service import currency_service
from app.api.financial_summary.service import recalculate_monthly_summary

class WalletService:

    @staticmethod
    def get_user_total_balance(db: Session, user):
        wallets = db.query(Wallet).filter(Wallet.user_id == user.id).all()
        total = Decimal("0.0")
        breakdown = []

        for wallet in wallets:
            if wallet.currency.upper() == user.default_currency.upper():
                converted = wallet.balance
                rate = 1.0
            else:
                converted = currency_service.convert_amount(
                    wallet.balance, wallet.currency, user.default_currency
                )
                rate = currency_service.get_exchange_rate(
                    wallet.currency, user.default_currency
                )

            total += converted

            breakdown.append({
                "wallet_id": wallet.id,
                "wallet_name": wallet.name,
                "wallet_type": wallet.wallet_type.value,
                "original_balance": float(wallet.balance),
                "original_currency": wallet.currency,
                "converted_balance": float(converted),
                "converted_currency": user.default_currency,
                "exchange_rate_used": float(rate),
            })

        return {
            "total_balance": float(total),
            "currency": user.default_currency,
            "breakdown": breakdown,
        }
    

    @staticmethod
    def create_wallet(db: Session, user, data):
        existing = db.query(Wallet).filter(
            Wallet.user_id == user.id,
            Wallet.name == data.name
        ).first()

        if existing:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Wallet with name '{data.name}' already exists"
            )

        wallet = Wallet(
            name=data.name,
            currency=data.currency,
            wallet_type=data.wallet_type,
            card_number=data.card_number,
            color=data.color,
            user_id=user.id,
            balance=data.balance
        )

        db.add(wallet)
        db.commit()
        db.refresh(wallet)
        return wallet


    @staticmethod
    def get_wallets(db: Session, user):
        return db.query(Wallet).filter(Wallet.user_id == user.id).all()


    @staticmethod
    def get_wallet(db: Session, user, wallet_id: int):
        wallet = db.query(Wallet).filter(
            Wallet.id == wallet_id,
            Wallet.user_id == user.id
        ).first()

        if not wallet:
            raise HTTPException(status_code=404, detail="Wallet not found")

        return wallet
    

    @staticmethod
    def get_wallet_balance(db: Session, user, wallet_id: int):
        wallet = WalletService.get_wallet(db, user, wallet_id)
        return {"balance": float(wallet.balance)}


    @staticmethod
    def update_wallet(db: Session, user, wallet_id: int, data):
        wallet = WalletService.get_wallet(db, user, wallet_id)

        # Name change validation
        if data.name is not None:
            existing = db.query(Wallet).filter(
                Wallet.user_id == user.id,
                Wallet.name == data.name,
                Wallet.id != wallet_id
            ).first()

            if existing:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Wallet with name '{data.name}' already exists"
                )

            wallet.name = data.name

        # Balance change
        if data.balance is not None:
            transactions = db.query(Transaction).filter(Transaction.wallet_id == wallet_id).all()
            net = Decimal("0.0")

            for t in transactions:
                net += t.amount if t.type.value == "income" else -t.amount

            new_balance = data.balance + net

            if new_balance < 0:
                raise HTTPException(
                    status_code=400,
                    detail=f"Cannot set initial balance to {data.balance}. "
                           f"Would result in negative balance ({new_balance})."
                )

            wallet.balance = new_balance

        # Currency change
        if data.currency is not None and data.currency != wallet.currency:
            wallet.balance = currency_service.convert_amount(
                wallet.balance, wallet.currency, data.currency
            )
            wallet.currency = data.currency

        # Optional fields
        if data.wallet_type is not None:
            wallet.wallet_type = data.wallet_type

        if data.card_number is not None:
            wallet.card_number = data.card_number

        if data.color is not None:
            wallet.color = data.color

        db.commit()
        db.refresh(wallet)
        return wallet
    

    @staticmethod
    def delete_wallet(db: Session, user, wallet_id: int):
        wallet = WalletService.get_wallet(db, user, wallet_id)

        db.query(Transaction).filter(Transaction.wallet_id == wallet_id).delete()

        db.delete(wallet)
        db.commit()

        recalculate_monthly_summary(db, user.id)