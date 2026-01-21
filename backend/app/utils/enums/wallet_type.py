import enum

class WalletType(enum.Enum):
    DEBIT_CARD = "debit_card"
    CASH = "cash"
    CREDIT_CARD = "credit_card"
    SAVING_ACCOUNT = "saving_account"
    INVESTMENT = "investment"
    LOAN = "loan"
    MORTGAGE = "mortgage"
    GOAL = "goal"
    