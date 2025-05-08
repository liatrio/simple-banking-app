package com.smartbank.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * InvestmentAccount extends Account with features specific to investment accounts.
 * Includes portfolio management, risk profiles, and investment tracking.
 */
@Entity
@DiscriminatorValue("Investment")
public class InvestmentAccount extends Account {
    
    @Column(name = "riskProfile")
    @Enumerated(EnumType.STRING)
    private RiskProfile riskProfile;
    
    @Column(name = "managementFeePercentage")
    private double managementFeePercentage;
    
    @Column(name = "lastFeeCollectionDate")
    private String lastFeeCollectionDate;
    
    @Column(name = "investmentStrategy")
    @Enumerated(EnumType.STRING)
    private InvestmentStrategy investmentStrategy;
    
    @Column(name = "portfolioAllocation")
    private String portfolioAllocationJson;
    
    @Transient
    private Map<AssetClass, Double> portfolioAllocation;
    
    @Column(name = "yearToDateReturn")
    private double yearToDateReturn;
    
    @Column(name = "inceptionToDateReturn")
    private double inceptionToDateReturn;
    
    @Column(name = "lastRebalanceDate")
    private String lastRebalanceDate;
    
    @Column(name = "tradingEnabled")
    private boolean tradingEnabled;
    
    @Column(name = "automatedRebalancingEnabled")
    private boolean automatedRebalancingEnabled;
    
    @Column(name = "taxAdvantaged")
    private boolean taxAdvantaged;
    
    @Column(name = "accountType")
    @Enumerated(EnumType.STRING)
    private InvestmentAccountType accountType;

    /**
     * Risk profile for investment strategy.
     */
    public enum RiskProfile {
        CONSERVATIVE,    // Lower risk, lower potential returns
        MODERATE,        // Balanced risk and potential returns
        AGGRESSIVE,      // Higher risk, higher potential returns
        CUSTOM           // Custom risk profile
    }
    
    /**
     * Investment strategy types.
     */
    public enum InvestmentStrategy {
        PASSIVE,         // Index-based passive investing
        ACTIVE,          // Actively managed portfolio
        VALUE,           // Value investing strategy
        GROWTH,          // Growth-focused strategy
        INCOME,          // Income-focused strategy
        CUSTOM           // Custom investment strategy
    }
    
    /**
     * Asset classes for portfolio allocation.
     */
    public enum AssetClass {
        CASH,            // Cash and cash equivalents
        BONDS,           // Fixed income securities
        STOCKS,          // Equity securities
        REAL_ESTATE,     // Real estate investments
        COMMODITIES,     // Commodity investments
        ALTERNATIVES     // Alternative investments
    }
    
    /**
     * Types of investment accounts.
     */
    public enum InvestmentAccountType {
        INDIVIDUAL,      // Individual investment account
        JOINT,           // Joint investment account
        RETIREMENT,      // Retirement account (IRA, 401k, etc.)
        EDUCATION,       // Education savings account
        TRUST            // Trust investment account
    }

    // Default constructor required by JPA
    protected InvestmentAccount() {
        super();
    }

    public InvestmentAccount(User accountHolder, double initialBalance) {
        super(accountHolder, initialBalance);
        this.riskProfile = RiskProfile.MODERATE;
        this.managementFeePercentage = 0.01; // Default 1% management fee
        this.lastFeeCollectionDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        this.investmentStrategy = InvestmentStrategy.PASSIVE;
        this.portfolioAllocation = createDefaultPortfolio();
        updatePortfolioAllocationJson();
        this.yearToDateReturn = 0.0;
        this.inceptionToDateReturn = 0.0;
        this.lastRebalanceDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        this.tradingEnabled = true;
        this.automatedRebalancingEnabled = true;
        this.taxAdvantaged = false;
        this.accountType = InvestmentAccountType.INDIVIDUAL;
    }

    public InvestmentAccount(User accountHolder, double initialBalance, RiskProfile riskProfile, 
                            InvestmentStrategy strategy) {
        super(accountHolder, initialBalance);
        this.riskProfile = riskProfile;
        this.managementFeePercentage = 0.01; // Default 1% management fee
        this.lastFeeCollectionDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        this.investmentStrategy = strategy;
        this.portfolioAllocation = createPortfolioByRiskProfile(riskProfile);
        updatePortfolioAllocationJson();
        this.yearToDateReturn = 0.0;
        this.inceptionToDateReturn = 0.0;
        this.lastRebalanceDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        this.tradingEnabled = true;
        this.automatedRebalancingEnabled = true;
        this.taxAdvantaged = false;
        this.accountType = InvestmentAccountType.INDIVIDUAL;
    }

    /**
     * Creates a default balanced portfolio allocation.
     * @return Map with default portfolio allocation
     */
    private Map<AssetClass, Double> createDefaultPortfolio() {
        Map<AssetClass, Double> portfolio = new HashMap<>();
        portfolio.put(AssetClass.CASH, 0.10);      // 10% cash
        portfolio.put(AssetClass.BONDS, 0.40);     // 40% bonds
        portfolio.put(AssetClass.STOCKS, 0.40);    // 40% stocks
        portfolio.put(AssetClass.REAL_ESTATE, 0.05); // 5% real estate
        portfolio.put(AssetClass.COMMODITIES, 0.03); // 3% commodities
        portfolio.put(AssetClass.ALTERNATIVES, 0.02); // 2% alternatives
        return portfolio;
    }

    /**
     * Creates a portfolio allocation based on risk profile.
     * @param profile The risk profile
     * @return Map with portfolio allocation based on risk profile
     */
    private Map<AssetClass, Double> createPortfolioByRiskProfile(RiskProfile profile) {
        Map<AssetClass, Double> portfolio = new HashMap<>();
        
        switch (profile) {
            case CONSERVATIVE:
                portfolio.put(AssetClass.CASH, 0.20);      // 20% cash
                portfolio.put(AssetClass.BONDS, 0.60);     // 60% bonds
                portfolio.put(AssetClass.STOCKS, 0.15);    // 15% stocks
                portfolio.put(AssetClass.REAL_ESTATE, 0.03); // 3% real estate
                portfolio.put(AssetClass.COMMODITIES, 0.01); // 1% commodities
                portfolio.put(AssetClass.ALTERNATIVES, 0.01); // 1% alternatives
                break;
            case MODERATE:
                portfolio.put(AssetClass.CASH, 0.10);      // 10% cash
                portfolio.put(AssetClass.BONDS, 0.40);     // 40% bonds
                portfolio.put(AssetClass.STOCKS, 0.40);    // 40% stocks
                portfolio.put(AssetClass.REAL_ESTATE, 0.05); // 5% real estate
                portfolio.put(AssetClass.COMMODITIES, 0.03); // 3% commodities
                portfolio.put(AssetClass.ALTERNATIVES, 0.02); // 2% alternatives
                break;
            case AGGRESSIVE:
                portfolio.put(AssetClass.CASH, 0.05);      // 5% cash
                portfolio.put(AssetClass.BONDS, 0.15);     // 15% bonds
                portfolio.put(AssetClass.STOCKS, 0.65);    // 65% stocks
                portfolio.put(AssetClass.REAL_ESTATE, 0.08); // 8% real estate
                portfolio.put(AssetClass.COMMODITIES, 0.05); // 5% commodities
                portfolio.put(AssetClass.ALTERNATIVES, 0.02); // 2% alternatives
                break;
            case CUSTOM:
            default:
                // Default to moderate if custom is selected without specific allocations
                return createPortfolioByRiskProfile(RiskProfile.MODERATE);
        }
        
        return portfolio;
    }

    /**
     * Updates the JSON string representation of the portfolio allocation map.
     */
    private void updatePortfolioAllocationJson() {
        if (portfolioAllocation == null || portfolioAllocation.isEmpty()) {
            portfolioAllocationJson = "{}";
            return;
        }
        
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<AssetClass, Double> entry : portfolioAllocation.entrySet()) {
            json.append("\"").append(entry.getKey().name()).append("\":");
            json.append(entry.getValue()).append(",");
        }
        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }
        json.append("}");
        portfolioAllocationJson = json.toString();
    }

    /**
     * Parses the JSON string representation of portfolio allocation into a map.
     */
    @PostLoad
    private void parsePortfolioAllocationJson() {
        if (portfolioAllocationJson == null || portfolioAllocationJson.equals("{}")) {
            portfolioAllocation = createDefaultPortfolio();
            return;
        }
        
        portfolioAllocation = new HashMap<>();
        
        // Simple parsing for our specific format - in a real app would use a JSON library
        String json = portfolioAllocationJson.replaceAll("[{}\"]", "");
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                try {
                    AssetClass assetClass = AssetClass.valueOf(keyValue[0].trim());
                    double allocation = Double.parseDouble(keyValue[1].trim());
                    portfolioAllocation.put(assetClass, allocation);
                } catch (IllegalArgumentException e) {
                    // Skip invalid entries
                }
            }
        }
    }

    public RiskProfile getRiskProfile() {
        return riskProfile;
    }

    public void setRiskProfile(RiskProfile riskProfile) {
        this.riskProfile = riskProfile;
    }

    public double getManagementFeePercentage() {
        return managementFeePercentage;
    }

    public void setManagementFeePercentage(double managementFeePercentage) {
        this.managementFeePercentage = managementFeePercentage;
    }

    public String getLastFeeCollectionDate() {
        return lastFeeCollectionDate;
    }

    public LocalDate getLastFeeCollectionLocalDate() {
        if (lastFeeCollectionDate == null) return null;
        return LocalDate.parse(lastFeeCollectionDate);
    }

    public void setLastFeeCollectionDate(String lastFeeCollectionDate) {
        this.lastFeeCollectionDate = lastFeeCollectionDate;
    }
    
    public void setLastFeeCollectionDate(LocalDate lastFeeCollectionDate) {
        this.lastFeeCollectionDate = lastFeeCollectionDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public InvestmentStrategy getInvestmentStrategy() {
        return investmentStrategy;
    }

    public void setInvestmentStrategy(InvestmentStrategy investmentStrategy) {
        this.investmentStrategy = investmentStrategy;
    }

    public Map<AssetClass, Double> getPortfolioAllocation() {
        if (portfolioAllocation == null && portfolioAllocationJson != null) {
            parsePortfolioAllocationJson();
        }
        return portfolioAllocation;
    }

    public void setPortfolioAllocation(Map<AssetClass, Double> portfolioAllocation) {
        this.portfolioAllocation = portfolioAllocation;
        updatePortfolioAllocationJson();
    }

    public String getPortfolioAllocationJson() {
        return portfolioAllocationJson;
    }

    public double getYearToDateReturn() {
        return yearToDateReturn;
    }

    public void setYearToDateReturn(double yearToDateReturn) {
        this.yearToDateReturn = yearToDateReturn;
    }

    public double getInceptionToDateReturn() {
        return inceptionToDateReturn;
    }

    public void setInceptionToDateReturn(double inceptionToDateReturn) {
        this.inceptionToDateReturn = inceptionToDateReturn;
    }

    public String getLastRebalanceDate() {
        return lastRebalanceDate;
    }

    public LocalDate getLastRebalanceLocalDate() {
        if (lastRebalanceDate == null) return null;
        return LocalDate.parse(lastRebalanceDate);
    }

    public void setLastRebalanceDate(String lastRebalanceDate) {
        this.lastRebalanceDate = lastRebalanceDate;
    }
    
    public void setLastRebalanceDate(LocalDate lastRebalanceDate) {
        this.lastRebalanceDate = lastRebalanceDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public boolean isTradingEnabled() {
        return tradingEnabled;
    }

    public void setTradingEnabled(boolean tradingEnabled) {
        this.tradingEnabled = tradingEnabled;
    }

    public boolean isAutomatedRebalancingEnabled() {
        return automatedRebalancingEnabled;
    }

    public void setAutomatedRebalancingEnabled(boolean automatedRebalancingEnabled) {
        this.automatedRebalancingEnabled = automatedRebalancingEnabled;
    }

    public boolean isTaxAdvantaged() {
        return taxAdvantaged;
    }

    public void setTaxAdvantaged(boolean taxAdvantaged) {
        this.taxAdvantaged = taxAdvantaged;
    }

    public InvestmentAccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(InvestmentAccountType accountType) {
        this.accountType = accountType;
    }

    /**
     * Apply the management fee to the account.
     * @return The amount of the fee applied
     */
    public double applyManagementFee() {
        // Calculate the quarterly fee (typical for investment accounts)
        double feeAmount = balance * (managementFeePercentage / 4); // Quarterly fee
        balance -= feeAmount;
        lastFeeCollectionDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        return feeAmount;
    }

    /**
     * Rebalance the portfolio to match the target allocation.
     * In a real system, this would involve selling and buying assets.
     * This is a simplified simulation.
     * @return true if rebalancing was successful
     */
    public boolean rebalancePortfolio() {
        // In a real system, this would execute trades to rebalance
        // For simulation, we'll just update the rebalance date
        lastRebalanceDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        return true;
    }

    /**
     * Simulate a market change and update the account balance accordingly.
     * @param marketChangePercent The percentage change in the market (positive or negative)
     * @return The new account balance
     */
    public double simulateMarketChange(double marketChangePercent) {
        if (!tradingEnabled) {
            return balance;
        }
        
        // Calculate the weighted change based on portfolio allocation
        double weightedChange = 0.0;
        Map<AssetClass, Double> allocation = getPortfolioAllocation();
        
        // Each asset class would have different sensitivities to market changes
        // This is a simplified example
        for (Map.Entry<AssetClass, Double> entry : allocation.entrySet()) {
            double assetAllocation = entry.getValue();
            double assetSensitivity = getAssetSensitivity(entry.getKey());
            weightedChange += assetAllocation * assetSensitivity * marketChangePercent;
        }
        
        // Apply the change to the account balance
        double changeAmount = balance * (weightedChange / 100.0);
        balance += changeAmount;
        
        // Update returns
        if (yearToDateReturn == 0.0) {
            yearToDateReturn = weightedChange;
        } else {
            // Compound the returns
            yearToDateReturn = ((1 + (yearToDateReturn / 100.0)) * (1 + (weightedChange / 100.0)) - 1) * 100.0;
        }
        
        if (inceptionToDateReturn == 0.0) {
            inceptionToDateReturn = weightedChange;
        } else {
            // Compound the returns
            inceptionToDateReturn = ((1 + (inceptionToDateReturn / 100.0)) * (1 + (weightedChange / 100.0)) - 1) * 100.0;
        }
        
        return balance;
    }

    /**
     * Get the sensitivity of an asset class to market changes.
     * Higher value means more sensitive to market changes.
     * @param assetClass The asset class
     * @return The sensitivity factor
     */
    private double getAssetSensitivity(AssetClass assetClass) {
        switch (assetClass) {
            case CASH:
                return 0.1; // Very low sensitivity
            case BONDS:
                return 0.5; // Low sensitivity
            case STOCKS:
                return 1.5; // High sensitivity
            case REAL_ESTATE:
                return 1.0; // Medium sensitivity
            case COMMODITIES:
                return 1.2; // Medium-high sensitivity
            case ALTERNATIVES:
                return 0.8; // Medium-low sensitivity
            default:
                return 1.0; // Default medium sensitivity
        }
    }

    @Override
    public void withdraw(double amount) throws Exception {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        
        if (amount > balance) {
            throw new Exception("Insufficient funds in investment account.");
        }
        
        // Investment accounts often have withdrawal penalties or tax implications
        // This is a simplified implementation
        balance -= amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InvestmentAccount that = (InvestmentAccount) o;
        return Double.compare(that.managementFeePercentage, managementFeePercentage) == 0 &&
               riskProfile == that.riskProfile &&
               investmentStrategy == that.investmentStrategy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), riskProfile, managementFeePercentage, investmentStrategy);
    }

    @Override
    public String toString() {
        return "InvestmentAccount{" +
                "accountNumber=" + getAccountNumber() +
                ", accountHolder=" + getAccountHolder().getUsername() +
                ", balance=" + getBalance() +
                ", riskProfile=" + riskProfile +
                ", investmentStrategy=" + investmentStrategy +
                ", yearToDateReturn=" + yearToDateReturn + "%" +
                ", accountType=" + accountType +
                '}';
    }
}