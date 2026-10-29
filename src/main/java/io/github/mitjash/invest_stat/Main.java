package io.github.mitjash.invest_stat;

import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc;
import ru.tinkoff.piapi.contract.v1.OperationItem;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        Waiter waiter = Waiter.getInstance();

        //Записываем в Postgres позиции
        DatabaseConnection.getInstance().insertMyPositions(TBankConnection.getInstance().getMyPositions(null));

        //Записываем в Postgres старые инструменты
        //DatabaseConnection.getInstance().insertOldInstuments(DatabaseConnection.getInstance().getUnkownInstuments());

        //Записываем в Postgres старые операции
        //DatabaseConnection.getInstance().insertOperations(TBankConnection.getInstance().getOperations(Instant.parse("2023-01-01T00:00:00Z"), Instant.now()));

        //Записываем в Postgres новые операции
        DatabaseConnection.getInstance().insertOperations(TBankConnection.getInstance().getOperations());


        //TBankConnection.getInstance().getAccounts();

        //System.out.println(TBankConnection.getInstance().getMyPositions("2303224778")); //ЦФА
        //System.out.println(TBankConnection.getInstance().getMyPositions("2006886721"));
        //System.out.println(TBankConnection.getInstance().getMyPositions(null));

        //System.out.println(TBankConnection.searchInstrument(TBankConnection.getInstance().getInstrumentsService(), "012b58d9-3b4d-4867-9f8f-bed77781ea35"));
        //System.out.println(TBankConnection.searchInstrument(TBankConnection.getInstance().getInstrumentsService(), "404e7d85-7cf1-4e7d-a8b4-ebb49bd63d11"));
        //System.out.println(DatabaseConnection.getInstance().getUnkownInstuments());

        //System.out.println(TBankConnection.getInstance().getOperations(Instant.parse("2025-09-16T11:08:44Z"), Instant.parse("2025-09-16T11:08:46Z")));

    }
}