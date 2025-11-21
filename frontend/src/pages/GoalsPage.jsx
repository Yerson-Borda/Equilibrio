import React, { useState } from 'react';
import AppLayout from '../components/layout/AppLayout';
import Card from '../components/ui/Card';
import BudgetProgress from '../components/budget/BudgetProgress';
import BudgetSetup from '../components/budget/BudgetSetup';

const GoalsPage = () => {
    const [budgetState, setBudgetState] = useState({
        budget: 0,
        spent: 0,
        month: new Date().toISOString().slice(0, 7),
    });

    const handleBudgetUpdate = ({ budget, spent, month }) => {
        setBudgetState({
            budget: Number(budget) || 0,
            spent: Number(spent) || 0,
            month,
        });
    };

    return (
        <AppLayout activeItem="goals">
            <div className="max-w-7xl mx-auto space-y-8">
                <h1 className="text-2xl font-bold text-text mb-2">Goals</h1>
                <p className="text-sm text-metallic-gray mb-4">
                    Track your monthly budget and long-term savings goals here.
                </p>

                {/* Top row: savings placeholders */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    <Card title="Saving Goal">
                        <p className="text-metallic-gray text-sm">
                            You can connect this block to a real savings goal system later
                            (e.g. &quot;New Laptop&quot;, &quot;Vacation Fund&quot;).
                        </p>
                    </Card>

                    <Card title="Saving Summary">
                        <p className="text-metallic-gray text-sm">
                            This can later display charts of your monthly savings trend,
                            goal completion percentage, and projected timelines.
                        </p>
                    </Card>
                </div>

                {/* Budget section */}
                <div className="grid grid-cols-1 xl:grid-cols-[1.2fr,1.4fr] gap-6">
                    <Card title="Budget Progress">
                        <BudgetProgress
                            budget={budgetState.budget}
                            spent={budgetState.spent}
                            month={budgetState.month}
                        />
                    </Card>

                    <Card title="Budget Setup">
                        <BudgetSetup onBudgetUpdate={handleBudgetUpdate} />
                    </Card>
                </div>

                {/* Future goals area */}
                <Card title="Long-term Goals">
                    <p className="text-metallic-gray text-sm">
                        Here you can later show cards for individual goals (e.g.
                        &quot;Trip to Iceland&quot;, &quot;Emergency Fund&quot;) using your
                        backend once those endpoints exist.
                    </p>
                </Card>
            </div>
        </AppLayout>
    );
};

export default GoalsPage;
