package io.github.mitjash.invest_stat;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import ru.ttech.piapi.core.connector.ConnectorConfiguration;
import ru.ttech.piapi.core.connector.ServiceStubFactory;
import ru.tinkoff.piapi.contract.v1.*;
import ru.ttech.piapi.core.connector.SyncStubWrapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Properties;

public class TBankConnection {

    private static final TBankConnection INSTANCE = new TBankConnection();

    private SyncStubWrapper<InstrumentsServiceGrpc.InstrumentsServiceBlockingStub> my_instrumentsService;
    private SyncStubWrapper<OperationsServiceGrpc.OperationsServiceBlockingStub> my_operationsService;
    private SyncStubWrapper<ru.tinkoff.piapi.contract.v1.UsersServiceGrpc.UsersServiceBlockingStub> my_usersService;

    private SyncStubWrapper<InstrumentsServiceGrpc.InstrumentsServiceBlockingStub> mamas_instrumentsService;
    private SyncStubWrapper<OperationsServiceGrpc.OperationsServiceBlockingStub> mamas_operationsService;
    private SyncStubWrapper<ru.tinkoff.piapi.contract.v1.UsersServiceGrpc.UsersServiceBlockingStub> mamas_usersService;

    private int instruments_req_cnt; // Share InstrumentRequests between two connectors


    // Конструктор
    private TBankConnection() {
        instruments_req_cnt = 0;
        // Безопасно извлекаем токен из переменных окружения
        String my_token = System.getenv("MY_INVEST_TOKEN");
        if (my_token == null || my_token.isEmpty()) {
            System.err.println("Error: Environment variable MY_INVEST_TOKEN is not set!");
            return;
        } else {
            Properties my_props = new Properties();
            my_props.setProperty("token", my_token);
            ConnectorConfiguration my_configuration = ConnectorConfiguration.loadFromProperties(my_props);
            ServiceStubFactory my_connector = ServiceStubFactory.create(my_configuration);
            my_instrumentsService = my_connector.newSyncService(InstrumentsServiceGrpc::newBlockingStub);
            my_operationsService = my_connector.newSyncService(OperationsServiceGrpc::newBlockingStub);
            my_usersService = my_connector.newSyncService(UsersServiceGrpc::newBlockingStub);
        }


        String mamas_token = System.getenv("MAMAS_INVEST_TOKEN");
        if (mamas_token == null || mamas_token.isEmpty()) {
            System.err.println("Error: Environment variable MAMAS_INVEST_TOKEN is not set!");
            return;
        } else {
            Properties mamas_props = new Properties();
            mamas_props.setProperty("token", mamas_token);
            ConnectorConfiguration mamas_configuration = ConnectorConfiguration.loadFromProperties(mamas_props);
            ServiceStubFactory  mamas_connector = ServiceStubFactory.create(mamas_configuration);
            mamas_instrumentsService = mamas_connector.newSyncService(InstrumentsServiceGrpc::newBlockingStub);
            mamas_operationsService = mamas_connector.newSyncService(OperationsServiceGrpc::newBlockingStub);
            mamas_usersService = mamas_connector.newSyncService(UsersServiceGrpc::newBlockingStub);
        }
    }

    public static TBankConnection getInstance() {
        return INSTANCE;
    }

    public SyncStubWrapper<InstrumentsServiceGrpc.InstrumentsServiceBlockingStub> getInstrumentsService() {
        if (++instruments_req_cnt % 2 == 0) {
            Waiter.getInstance().checkLimits("MyInstrumentsService");
            return my_instrumentsService;
        } else {
            Waiter.getInstance().checkLimits("MamasInstrumentsService");
            return mamas_instrumentsService;
        }
    }

    // --- ОБЫЧНЫЕ МЕТОДЫ КЛАССА ---

    public Instrument getInstrument(String p_instrumentUid)  {
        InstrumentResponse response;

        InstrumentRequest request = InstrumentRequest.newBuilder()
                .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_UID)
                .setId(p_instrumentUid)
                .build();
        // Share InstrumentRequests between two connectors
        if (++instruments_req_cnt % 2 == 0) {
            Waiter.getInstance().checkLimits("MyInstrumentsService");
            response = my_instrumentsService.getStub().getInstrumentBy(request);
        } else {
            Waiter.getInstance().checkLimits("MamasInstrumentsService");
            response = mamas_instrumentsService.getStub().getInstrumentBy(request);
        }

        return response.getInstrument();
    }

    public InstrumentShort findInstrument(String p_instrumentUid)  {
        FindInstrumentResponse response;

        FindInstrumentRequest  request = FindInstrumentRequest.newBuilder()
                .setQuery(p_instrumentUid)
                .build();
        // Share InstrumentRequests between two connectors
        if (++instruments_req_cnt % 2 == 0) {
            Waiter.getInstance().checkLimits("MyInstrumentsService");
            response = my_instrumentsService.getStub().findInstrument(request);
        } else {
            Waiter.getInstance().checkLimits("MamasInstrumentsService");
            response = mamas_instrumentsService.getStub().findInstrument(request);
        }

        // КРИТИЧЕСКИЙ ШАГ: проверяем, что брокер хоть что-то нашел
        if (response.getInstrumentsCount() == 0) {
            System.out.println("Cant find instument for " + p_instrumentUid);
            return null; // или возвращайте заглушку
        } else {
            return response.getInstruments(0);
        }
    }

    public AssetFull getAsset(String p_AssetUid)  {
        AssetResponse response;
        AssetRequest request = AssetRequest.newBuilder()
                .setId(p_AssetUid)
                .build();
        try {
            // Share InstrumentRequests between two connectors
            if (++instruments_req_cnt % 2 == 0) {
                Waiter.getInstance().checkLimits("MyInstrumentsService");
                response = my_instrumentsService.getStub().getAssetBy(request);
            } else {
                Waiter.getInstance().checkLimits("MamasInstrumentsService");
                response = mamas_instrumentsService.getStub().getAssetBy(request);
            }

            return response.getAsset();
        } catch (StatusRuntimeException e) {
            return null;
        }
    }

    public Bond getBond(String p_instrumentUid)  {
        BondResponse response;
        InstrumentRequest request = InstrumentRequest.newBuilder()
                .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_UID)
                .setId(p_instrumentUid)
                .build();
        try {
            // Share InstrumentRequests between two connectors
            if (++instruments_req_cnt % 2 == 0) {
                Waiter.getInstance().checkLimits("MyInstrumentsService");
                response = my_instrumentsService.callSyncMethod(stub -> stub.bondBy(request));
            } else {
                Waiter.getInstance().checkLimits("MamasInstrumentsService");
                response = mamas_instrumentsService.callSyncMethod(stub -> stub.bondBy(request));
            }

            return response.getInstrument();
        } catch (StatusRuntimeException e) {
            return null;
        }
    }

    public ArrayList<MyPosition> getMyPositions(String p_accout_id)  {
        ArrayList<MyPosition> result = new ArrayList<MyPosition>();

        // My accounts
        Waiter.getInstance().checkLimits("MyUsersService");
        GetAccountsRequest my_acc_request = GetAccountsRequest.getDefaultInstance();
        GetAccountsResponse my_acc_response = my_usersService.getStub().getAccounts(my_acc_request);

        for (var account : my_acc_response.getAccountsList()) {
            if (p_accout_id != null && !p_accout_id.equals(account.getId())) {
                continue;
            }

            PortfolioRequest request = PortfolioRequest.newBuilder()
                    .setAccountId(account.getId())
                    .build();

            Waiter.getInstance().checkLimits("MyOperationsService");
            PortfolioResponse response = my_operationsService.getStub().getPortfolio(request);

            PositionsRequest request2 = PositionsRequest.newBuilder()
                    .setAccountId(account.getId())
                    .build();

            // Перебираем все купленные ценные бумаги
            for (PortfolioPosition position : response.getPositionsList()) {
                result.add(new MyPosition(position.getFigi(),
                        position.getInstrumentUid(),
                        position.getInstrumentType(),
                        account.getId(),
                        position.getQuantity(),
                        position.getBlockedLots(),
                        position.getCurrentPrice(),
                        position.getAveragePositionPrice(),
                        position.getBlocked()));
            }
        }

        // Mamas accounts
        Waiter.getInstance().checkLimits("MamasUsersService");
        GetAccountsRequest mamas_acc_request = GetAccountsRequest.getDefaultInstance();
        GetAccountsResponse mamas_acc_response = mamas_usersService.getStub().getAccounts(mamas_acc_request);

        for (var account : mamas_acc_response.getAccountsList()) {
            if (p_accout_id!=null && !p_accout_id.equals(account.getId())) {
                continue;
            }

            PortfolioRequest request = PortfolioRequest.newBuilder()
                    .setAccountId(account.getId())
                    .build();

            Waiter.getInstance().checkLimits("MamasOperationsService");
            PortfolioResponse response = mamas_operationsService.getStub().getPortfolio(request);

            PositionsRequest request2 = PositionsRequest.newBuilder()
                    .setAccountId(account.getId())
                    .build();

            // Перебираем все купленные ценные бумаги
            for (var position : response.getPositionsList()) {
                result.add(new MyPosition(position.getFigi(),
                                position.getInstrumentUid(),
                                position.getInstrumentType(),
                                account.getId(),
                                position.getQuantity(),
                                position.getBlockedLots(),
                                position.getCurrentPrice(),
                                position.getAveragePositionPrice(),
                                position.getBlocked()));
            }
        }

        return result;
    }

    public ArrayList<OperationItem> getOperations() {
        Instant from_time = DatabaseConnection.getInstance().getLastOrationTime().plusSeconds(1); // Correct with TimeZone
        Instant to_time = Instant.now();
        System.out.println("From: " + from_time + ", to: " + to_time);
        return getOperations(from_time, to_time);
    }


    public ArrayList<OperationItem> getOperations(Instant p_time_from, Instant p_time_to)  {
        ArrayList<OperationItem> result = new ArrayList<OperationItem>();

        // Adjust with Moscow TimeZone
        Instant time_from = p_time_from;
        Instant time_to = p_time_to;
        //Instant time_from = p_time_from.minus(3, ChronoUnit.HOURS);
        //Instant time_to = p_time_to.minus(3, ChronoUnit.HOURS);

        // My accounts
        Waiter.getInstance().checkLimits("MyUsersService");
        GetAccountsRequest my_acc_request = GetAccountsRequest.getDefaultInstance();
        GetAccountsResponse my_acc_response = my_usersService.getStub().getAccounts(my_acc_request);

        for (var account : my_acc_response.getAccountsList()) {

            String nextCursor = ""; // Стартуем с пустой строки

            while (true) {
                // Формируем запрос с текущим курсором
                GetOperationsByCursorRequest.Builder requestBuilder = GetOperationsByCursorRequest.newBuilder()
                        .setAccountId(account.getId())
                        .setFrom(Timestamp.newBuilder().setSeconds(time_from.getEpochSecond()).build())
                        .setTo(Timestamp.newBuilder().setSeconds(time_to.getEpochSecond()).build())
                        .setLimit(100); // Запрашиваем пачками по 100

                // Если это не первая итерация, передаем маркер следующей страницы
                if (!nextCursor.isEmpty()) {
                    requestBuilder.setCursor(nextCursor);
                }

                // Вызов gRPC метода
                Waiter.getInstance().checkLimits("MyOperationsService");
                GetOperationsByCursorResponse response = my_operationsService.getStub().getOperationsByCursor(requestBuilder.build());

                // Сохраняем полученные операции
                result.addAll(response.getItemsList());

                // Проверяем, есть ли следующая страница
                if (response.getHasNext()) {
                    nextCursor = response.getNextCursor(); // Обновляем курсор для следующего шага
                } else {
                    break; // Если следующей страницы нет, выходим из цикла
                }
            }
        }

        // Mamas accounts
        Waiter.getInstance().checkLimits("MamasUsersService");
        GetAccountsRequest mamas_acc_request = GetAccountsRequest.getDefaultInstance();
        GetAccountsResponse mamas_acc_response = mamas_usersService.getStub().getAccounts(mamas_acc_request);

        for (var account : mamas_acc_response.getAccountsList()) {

            String nextCursor = ""; // Стартуем с пустой строки

            while (true) {
                // Формируем запрос с текущим курсором
                GetOperationsByCursorRequest.Builder requestBuilder = GetOperationsByCursorRequest.newBuilder()
                        .setAccountId(account.getId())
                        .setFrom(Timestamp.newBuilder().setSeconds(time_from.getEpochSecond()).build())
                        .setTo(Timestamp.newBuilder().setSeconds(time_to.getEpochSecond()).build())
                        .setLimit(100); // Запрашиваем пачками по 100

                // Если это не первая итерация, передаем маркер следующей страницы
                if (!nextCursor.isEmpty()) {
                    requestBuilder.setCursor(nextCursor);
                }

                // Вызов gRPC метода
                Waiter.getInstance().checkLimits("MamasOperationsService");
                GetOperationsByCursorResponse response = mamas_operationsService.getStub().getOperationsByCursor(requestBuilder.build());

                // Сохраняем полученные операции
                result.addAll(response.getItemsList());

                // Проверяем, есть ли следующая страница
                if (response.getHasNext()) {
                    nextCursor = response.getNextCursor(); // Обновляем курсор для следующего шага
                } else {
                    break; // Если следующей страницы нет, выходим из цикла
                }
            }
        }
        System.out.println("Total operation got: " + result.size());

        return result;
    }

    public void getAccounts()  {

        // My accounts
        Waiter.getInstance().checkLimits("MyUsersService");
        GetAccountsRequest my_acc_request = GetAccountsRequest.getDefaultInstance();
        GetAccountsResponse my_acc_response = my_usersService.getStub().getAccounts(my_acc_request);

        for (var account : my_acc_response.getAccountsList()) {
            System.out.printf("Аккаунт ID: %s, Имя: %s%n", account.getId(), account.getName());

            // Запрашиваем состояние портфеля для каждого аккаунта
            Waiter.getInstance().checkLimits("MyOperationsService");
            PortfolioRequest request = PortfolioRequest.newBuilder()
                    .setAccountId(account.getId())
                    .build();

            PortfolioResponse response = my_operationsService.getStub().getPortfolio(request);

            System.out.println("Общая стоимость портфеля: " + response.getTotalAmountPortfolio().getUnits());
            System.out.println("Текущие позиции: " + response.getPositionsList().size());
            System.out.println("----------------------------------------");
        }

        System.out.println("=== Mamas accounts ===");
        Waiter.getInstance().checkLimits("MamasUsersService");
        GetAccountsRequest mamas_acc_request = GetAccountsRequest.getDefaultInstance();
        GetAccountsResponse mamas_acc_response = mamas_usersService.getStub().getAccounts(mamas_acc_request);

        for (var account : mamas_acc_response.getAccountsList()) {
            System.out.printf("Аккаунт ID: %s, Имя: %s%n", account.getId(), account.getName());

            // Запрашиваем состояние портфеля для каждого аккаунта
            Waiter.getInstance().checkLimits("MamasOperationsService");
            PortfolioRequest request = PortfolioRequest.newBuilder()
                    .setAccountId(account.getId())
                    .build();

            PortfolioResponse response = mamas_operationsService.getStub().getPortfolio(request);

            System.out.println("Общая стоимость портфеля: " + response.getTotalAmountPortfolio().getUnits());
            System.out.println("Текущие позиции: " + response.getPositionsList().size());
            System.out.println("----------------------------------------");
        }
    }

    public static BigDecimal toBigDecimal(MoneyValue value) {
        if (value == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(value.getUnits())
                .add(BigDecimal.valueOf(value.getNano(), 9));
    }

    // Static Methods
    public static String searchInstrument(SyncStubWrapper<InstrumentsServiceGrpc.InstrumentsServiceBlockingStub> instrumentsWrapper,
                                    String instrumentUid) {
        try {
            FindInstrumentRequest request = FindInstrumentRequest.newBuilder()
                    .setQuery(instrumentUid)
                    .build();

            // ИСПРАВЛЕНИЕ: Передаем лямбду (stub -> stub.метод) вместо ссылки на класс
            FindInstrumentResponse response = instrumentsWrapper.callSyncMethod(
                    stub -> stub.findInstrument(request)
            );

            if (response.getInstrumentsCount() > 0) {
                InstrumentShort dfaInfo = response.getInstruments(0);
                return dfaInfo.getName(); // Возвращаем имя
            }

            return "Имя не найдено в поисковом индексе";

        } catch (io.grpc.StatusRuntimeException e) {
            return "Ошибка gRPC: " + e.getStatus().getCode();
        }
    }
}

