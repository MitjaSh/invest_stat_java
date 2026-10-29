package io.github.mitjash.invest_stat;


import ru.tinkoff.piapi.contract.v1.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;

public class MyPosition {
    private String figi;
    private String instrumentType;
    private String accountId;
    private int quantity;
    private int blockedQuantity;
    private MoneyValue currentPrice;
    private MoneyValue averagePrice;
    private String name;
    private String ticker;
    private String isin;
    private String brand;
    private String assetUid;
    private String instrumentUid;
    boolean blocked;
    private int tradingStatus;
    private int bondType;
    private MoneyValue bondNominal;
    private String bondSector;
    private Date bondMaturityDate;
    private Date bondCallPutDate;
    private String ccy;


    // Конструктор
    public MyPosition(String p_figi,
                      String p_instrumentUid,
                      String p_instrumentType,
                      String p_accountId,
                      Quotation p_quantity,
                      Quotation p_blockedQuantity,
                      MoneyValue p_currentPrice,
                      MoneyValue p_averagePrice,
                      Boolean p_blocked) {
        figi = p_figi;
        instrumentUid = p_instrumentUid;
        instrumentType = p_instrumentType;
        accountId = p_accountId;
        quantity = (int) p_quantity.getUnits();
        blockedQuantity = (int) p_blockedQuantity.getUnits();
        currentPrice = p_currentPrice;
        averagePrice = p_averagePrice;
        blocked = p_blocked;
        Instrument  instrument = TBankConnection.getInstance().getInstrument(instrumentUid);

        name =  instrument.getName();
        ticker = instrument.getTicker();
        isin = instrument.getIsin();
        assetUid = instrument.getAssetUid();
        tradingStatus = instrument.getTradingStatus().getNumber();
        ccy = instrument.getCurrency();

        AssetFull asset = TBankConnection.getInstance().getAsset(assetUid);
        if (asset != null) {
            brand = asset.getBrand().getName();
        }

        if (instrumentType.equals("bond")) {
            Bond bond = TBankConnection.getInstance().getBond(instrumentUid);
            bondNominal = bond.getNominal();
            bondType = bond.getBondType().getNumber();
            bondSector = bond.getSector();
            bondMaturityDate = new java.sql.Date(Instant.ofEpochSecond(bond.getMaturityDate().getSeconds(), bond.getMaturityDate().getNanos()).toEpochMilli());
            bondCallPutDate =  new java.sql.Date(Instant.ofEpochSecond(bond.getCallDate().getSeconds(), bond.getCallDate().getNanos()).toEpochMilli());
        }

    }

    // Конструктор 2 - нет позиций
    public MyPosition(String p_instrumentUid) {

        instrumentUid = p_instrumentUid;
        accountId = null;



        try {
            Instrument  instrument = TBankConnection.getInstance().getInstrument(instrumentUid);
            name =  instrument.getName();
            ticker = instrument.getTicker();
            isin = instrument.getIsin();
            assetUid = instrument.getAssetUid();
            tradingStatus = instrument.getTradingStatus().getNumber();
            figi = instrument.getFigi();
            instrumentType = instrument.getInstrumentType();
            ccy = instrument.getCurrency();

            AssetFull asset = TBankConnection.getInstance().getAsset(assetUid);
            if (asset != null) {
                brand = asset.getBrand().getName();
            }
        } catch (Exception e) {
            System.out.println("Cant get instument for " + p_instrumentUid);
            InstrumentShort shortInstrument = TBankConnection.getInstance().findInstrument(instrumentUid);
            if (shortInstrument != null) {
                name =  shortInstrument.getName();
                ticker = shortInstrument.getTicker();
                isin = shortInstrument.getIsin();
                tradingStatus = 0;
                figi = shortInstrument.getFigi();
                instrumentType = shortInstrument.getInstrumentType();
            } else {
                return;
            }
        }

        if (instrumentType.equals("bond")) {
            Bond bond = TBankConnection.getInstance().getBond(instrumentUid);
            bondNominal = bond.getNominal();
            bondType = bond.getBondType().getNumber();
            bondSector = bond.getSector();
            bondMaturityDate = new java.sql.Date(Instant.ofEpochSecond(bond.getMaturityDate().getSeconds(), bond.getMaturityDate().getNanos()).toEpochMilli());
            bondCallPutDate =  new java.sql.Date(Instant.ofEpochSecond(bond.getCallDate().getSeconds(), bond.getCallDate().getNanos()).toEpochMilli());
        }

    }

    public String getAccountId() {
        return accountId;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public String getInstrumentUid() {
        return instrumentUid;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean getBlocked() {
        return blocked;
    }

    public BigDecimal getCurrentPrice() {
        return TBankConnection.toBigDecimal(currentPrice);
    }

    public BigDecimal getAveragePrice() {
        return TBankConnection.toBigDecimal(averagePrice);
    }

    public BigDecimal getNominal() {
        return TBankConnection.toBigDecimal(bondNominal);
    }

    public String getCcy() {
        return ccy;
    }

    public String getFigi() {
        return figi;
    }

    public String getTicker() {
        return ticker;
    }

    public String getIsin() {
        return isin;
    }

    public String getAssetUid() {
        return assetUid;
    }

    public int getBlockedQuantity() {
        return blockedQuantity;
    }

    public int getTradingStatus() {
        return tradingStatus;
    }

    public int getBondType() {
        return bondType;
    }

    public String getBondSector() {
        return bondSector;
    }

    public Date getBondMaturityDate() {
        return bondMaturityDate;
    }

    public Date getBondCallPutDate() {
        return bondCallPutDate;
    }


    public String toString(){
        return "{" + instrumentType + " \"" + name + "\"  (figi=" + figi + ", ticker=" + ticker + ", isin=" + isin + ", instrumentUid=" + instrumentUid + "), brand=\"" + brand + "\": " + quantity + " units in account " + accountId + " for " + TBankConnection.toBigDecimal(currentPrice) + " " + currentPrice.getCurrency()+ " TradingStatus=" + tradingStatus +"}";
    }
}
