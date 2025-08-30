package com.kaua.events.platform;

import com.kaua.events.platform.application.repositories.*;
import com.kaua.events.platform.infrastructure.eventmanagement.EventJdbcRepository;
import com.kaua.events.platform.infrastructure.jdbc.JdbcClientAdapter;
import com.kaua.events.platform.infrastructure.oauth.code.AuthorizationCodeJdbcRepository;
import com.kaua.events.platform.infrastructure.oauth.token.AuthorizationTokenJdbcRepository;
import com.kaua.events.platform.infrastructure.orders.OrderJdbcRepository;
import com.kaua.events.platform.infrastructure.organizations.OrganizationJdbcRepository;
import com.kaua.events.platform.infrastructure.organizations.OrganizationMemberJdbcRepository;
import com.kaua.events.platform.infrastructure.outbox.OutboxJdbcRepository;
import com.kaua.events.platform.infrastructure.payments.PaymentJdbcRepository;
import com.kaua.events.platform.infrastructure.ticket.TicketJdbcRepository;
import com.kaua.events.platform.infrastructure.users.UserJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

@DataJdbcTest
@Tag("integrationTest")
@ActiveProfiles("test-integration")
public abstract class AbstractRepositoryTest {

    private static final String USERS_TABLE = "users";
    private static final String AUTHORIZATION_CODE_TABLE = "authorization_codes";
    private static final String AUTHORIZATION_TOKEN_TABLE = "authorization_tokens";
    private static final String ORGANIZATIONS_TABLE = "organizations";
    private static final String ORGANIZATION_MEMBERS_TABLE = "organization_members";
    private static final String EVENTS_TABLE = "events";
    private static final String TICKETS_TABLE = "tickets";
    private static final String ORDERS_TABLE = "orders";
    private static final String ORDER_ITEMS_TABLE = "order_items";
    private static final String PAYMENTS_TABLE = "payments";
    private static final String OUTBOX_TABLE = "outbox";

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private NamedParameterJdbcOperations operations;

    private UserJdbcRepository userJdbcRepository;
    private AuthorizationCodeRepository authorizationCodeRepository;
    private AuthorizationTokenRepository authorizationTokenRepository;
    private OrganizationRepository organizationRepository;
    private OrganizationMemberRepository organizationMemberRepository;
    private EventRepository eventRepository;
    private TicketRepository ticketRepository;
    private OrderRepository orderRepository;
    private PaymentRepository paymentRepository;
    private OutboxJdbcRepository outboxJdbcRepository;

    @BeforeEach
    void setUp() {
        this.userJdbcRepository = new UserJdbcRepository(new JdbcClientAdapter(jdbcClient, operations));
        this.authorizationCodeRepository = new AuthorizationCodeJdbcRepository(new JdbcClientAdapter(jdbcClient, operations));
        this.authorizationTokenRepository = new AuthorizationTokenJdbcRepository(new JdbcClientAdapter(jdbcClient, operations));
        this.organizationRepository = new OrganizationJdbcRepository(new JdbcClientAdapter(jdbcClient, operations));
        this.organizationMemberRepository = new OrganizationMemberJdbcRepository(new JdbcClientAdapter(jdbcClient, operations));
        this.eventRepository = new EventJdbcRepository(new JdbcClientAdapter(jdbcClient, operations));
        this.ticketRepository = new TicketJdbcRepository(new JdbcClientAdapter(jdbcClient, operations));
        this.orderRepository = new OrderJdbcRepository(new JdbcClientAdapter(jdbcClient, operations));
        this.paymentRepository = new PaymentJdbcRepository(new JdbcClientAdapter(jdbcClient, operations));
        this.outboxJdbcRepository = new OutboxJdbcRepository(new JdbcClientAdapter(jdbcClient, operations));
    }

    protected int countUsers() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, USERS_TABLE);
    }

    protected int countAuthorizationCodes() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, AUTHORIZATION_CODE_TABLE);
    }

    protected int countAuthorizationTokens() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, AUTHORIZATION_TOKEN_TABLE);
    }

    protected int countOrganizations() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, ORGANIZATIONS_TABLE);
    }

    protected int countOrganizationMembers() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, ORGANIZATION_MEMBERS_TABLE);
    }

    protected int countEvents() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, EVENTS_TABLE);
    }

    protected int countTickets() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, TICKETS_TABLE);
    }

    protected int countOrders() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, ORDERS_TABLE);
    }

    protected int countOrderItems() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, ORDER_ITEMS_TABLE);
    }

    protected int countPayments() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, PAYMENTS_TABLE);
    }

    protected int countOutboxMessages() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, OUTBOX_TABLE);
    }

    public UserJdbcRepository userRepository() {
        return userJdbcRepository;
    }

    public AuthorizationCodeRepository authorizationCodeRepository() {
        return authorizationCodeRepository;
    }

    public AuthorizationTokenRepository authorizationTokenRepository() {
        return authorizationTokenRepository;
    }

    public OrganizationRepository organizationRepository() {
        return organizationRepository;
    }

    public OrganizationMemberRepository organizationMemberRepository() {
        return organizationMemberRepository;
    }

    public EventRepository eventRepository() {
        return eventRepository;
    }

    public TicketRepository ticketRepository() {
        return ticketRepository;
    }

    public OrderRepository orderRepository() {
        return orderRepository;
    }

    public PaymentRepository paymentRepository() {
        return paymentRepository;
    }

    public OutboxJdbcRepository outboxRepository() {
        return outboxJdbcRepository;
    }
}
