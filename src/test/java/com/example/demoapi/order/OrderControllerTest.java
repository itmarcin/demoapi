package com.example.demoapi.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static com.example.demoapi.DemoapiApplication.API_VERSION;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    public OrderEntityModelAssembler orderEntityModelAssembler;

    @MockBean
    private OrderRepository repository;

    ObjectMapper objectMapper = new ObjectMapper();

    private static final MediaType HAL_JSON = new MediaType("application", "hal+json");

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private EntityModel<Order> getMockedOrderEntityModel(Order order) {
        EntityModel<Order> orderModel = EntityModel.of(order,
                linkTo(methodOn(OrderController.class).getOrderById(order.getId())).withSelfRel(),
                linkTo(methodOn(OrderController.class).getOrders()).withRel("orders"));

        if (order.getStatus() == Status.IN_PROGRESS) {
            orderModel.add(linkTo(methodOn(OrderController.class).cancelOrder(order.getId())).withRel("cancel"));
            orderModel.add(linkTo(methodOn(OrderController.class).completeOrder(order.getId())).withRel("complete"));
        }
        return orderModel;
    }

    private CollectionModel<EntityModel<Order>> getMockedOrdersEntityModels(List<Order> orders) {
        List<EntityModel<Order>> modelOrders = orders.stream().map(this::getMockedOrderEntityModel).toList();
        return CollectionModel.of(modelOrders, linkTo(methodOn(OrderController.class).getOrders()).withSelfRel());
    }

    public ResponseEntity<?> getMockedMethodNotAllowedResponse(String message) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(Problem.create()
                        .withTitle("Method not allowed")
                        .withDetail(message));
    }

    @Test
    public void givenOrdersExist_whenGetOrders_thenReturnOrderListWithLinks() throws Exception {
        Order orderInProgress = new Order(1L, Status.IN_PROGRESS, "Meat");
        Order orderCompleted = new Order(2L, Status.COMPLETED, "Beans");
        List<Order> mockOrders = List.of(orderInProgress, orderCompleted);

        when(orderService.getOrders()).thenReturn(mockOrders);
        when(orderEntityModelAssembler.toCollectionModel(mockOrders)).thenReturn(getMockedOrdersEntityModels(mockOrders));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/" + API_VERSION + "/orders")
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.orderList").exists())
                .andExpect(jsonPath("$._embedded.orderList[*].id").isNotEmpty())
                .andExpect(jsonPath("$._embedded.orderList[0].id").value(orderInProgress.getId()))
                .andExpect(jsonPath("$._embedded.orderList[0].status").value(orderInProgress.getStatus().toString()))
                .andExpect(jsonPath("$._embedded.orderList[0].description").value(orderInProgress.getDescription()))
                .andExpect(jsonPath("$._embedded.orderList[0]._links.self.href").value("/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$._embedded.orderList[0]._links.orders.href").value("/" + API_VERSION + "/orders"))
                .andExpect(jsonPath("$._embedded.orderList[0]._links.cancel.href").value("/" + API_VERSION + "/orders/1/cancel"))
                .andExpect(jsonPath("$._embedded.orderList[0]._links.complete.href").value("/" + API_VERSION + "/orders/1/complete"))
                .andExpect(jsonPath("$._embedded.orderList[1].id").value(orderCompleted.getId()))
                .andExpect(jsonPath("$._embedded.orderList[1].status").value(orderCompleted.getStatus().toString()))
                .andExpect(jsonPath("$._embedded.orderList[1].description").value(orderCompleted.getDescription()))
                .andExpect(jsonPath("$._embedded.orderList[1]._links.self.href").value("/" + API_VERSION + "/orders/2"))
                .andExpect(jsonPath("$._embedded.orderList[1]._links.orders.href").value("/" + API_VERSION + "/orders"));
    }

    @Test
    public void givenOrderExists_whenGetOrderById_thenReturnOrderWithLinks() throws Exception {
        Order orderInProgress = new Order(1L, Status.IN_PROGRESS, "Meat");

        when(orderService.getOrderById(orderInProgress.getId())).thenReturn(orderInProgress);
        when(orderEntityModelAssembler.toModel(orderInProgress)).thenReturn(getMockedOrderEntityModel(orderInProgress));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/" + API_VERSION + "/orders/1")
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(orderInProgress.getId()))
                .andExpect(jsonPath("$.status").value(orderInProgress.getStatus().toString()))
                .andExpect(jsonPath("$.description").value(orderInProgress.getDescription()))
                .andExpect(jsonPath("$._links.self.href").value("/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$._links.orders.href").value("/" + API_VERSION + "/orders"))
                .andExpect(jsonPath("$._links.cancel.href").value("/" + API_VERSION + "/orders/1/cancel"))
                .andExpect(jsonPath("$._links.complete.href").value("/" + API_VERSION + "/orders/1/complete"));
    }

    @Test
    public void givenNewOrder_whenPostOrder_thenReturnSavedOrderWithLinks() throws Exception {
        Order orderInProgress = new Order(1L, Status.IN_PROGRESS, "Meat");

        when(orderEntityModelAssembler.toModel(orderInProgress)).thenReturn(getMockedOrderEntityModel(orderInProgress));
        when(orderService.saveOrder(orderInProgress)).thenReturn(orderInProgress);

        String jsonOrder = objectMapper.writeValueAsString(orderInProgress);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/" + API_VERSION + "/orders")
                        .contentType(HAL_JSON).content(jsonOrder)
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(orderInProgress.getId()))
                .andExpect(jsonPath("$.status").value(orderInProgress.getStatus().toString()))
                .andExpect(jsonPath("$.description").value(orderInProgress.getDescription()))
                .andExpect(jsonPath("$._links.self.href").value("/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$._links.orders.href").value("/" + API_VERSION + "/orders"))
                .andExpect(jsonPath("$._links.cancel.href").value("/" + API_VERSION + "/orders/1/cancel"))
                .andExpect(jsonPath("$._links.complete.href").value("/" + API_VERSION + "/orders/1/complete"));
    }

    @Test
    public void givenOrderExists_whenPutOrderWithNewParameters_thenReturnUpdatedOrderWithLinks() throws Exception {
        Order orderInProgress = new Order(1L, Status.IN_PROGRESS, "Meat");

        when(orderEntityModelAssembler.toModel(orderInProgress)).thenReturn(getMockedOrderEntityModel(orderInProgress));
        when(orderService.updateOrder(orderInProgress, 2L)).thenReturn(orderInProgress);

        String jsonOrder = objectMapper.writeValueAsString(orderInProgress);
        mockMvc.perform(MockMvcRequestBuilders
                        .put(("/" + API_VERSION + "/orders/2"), orderInProgress)
                        .content(jsonOrder)
                        .contentType(HAL_JSON)
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(orderInProgress.getId()))
                .andExpect(jsonPath("$.status").value(orderInProgress.getStatus().toString()))
                .andExpect(jsonPath("$.description").value(orderInProgress.getDescription()))
                .andExpect(jsonPath("$._links.self.href").value("/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$._links.orders.href").value("/" + API_VERSION + "/orders"))
                .andExpect(jsonPath("$._links.cancel.href").value("/" + API_VERSION + "/orders/1/cancel"))
                .andExpect(jsonPath("$._links.complete.href").value("/" + API_VERSION + "/orders/1/complete"));
    }

    @Test
    public void givenOrder_whenDeleted_thenReturnNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/" + API_VERSION + "/orders/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenOrderInProgress_whenCancel_thenReturnCancelledOrderWithLinks() throws Exception {
        Order orderInProgress = new Order(1L, Status.IN_PROGRESS, "Meat");
        Order orderCanceled = new Order(1L, Status.CANCELED, "Meat");

        when(orderService.cancelOrder(1L)).thenReturn(orderCanceled);
        when(orderService.isOrderInProgress(1L)).thenReturn(true);
        when(orderEntityModelAssembler.toModel(orderCanceled)).thenReturn(getMockedOrderEntityModel(orderCanceled));

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/" + API_VERSION + "/orders/1/cancel")
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(orderInProgress.getId()))
                .andExpect(jsonPath("$.status").value(orderCanceled.getStatus().toString()))
                .andExpect(jsonPath("$.description").value(orderInProgress.getDescription()))
                .andExpect(jsonPath("$._links.self.href").value("/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$._links.orders.href").value("/" + API_VERSION + "/orders"));
    }

    @Test
    public void givenOrderCompletedOrder_whenCancel_thenReturnMethodNotAllowed() throws Exception {
        when(orderService.isOrderInProgress(2L)).thenReturn(false);

        when(orderEntityModelAssembler.getMethodNotAllowedResponse("You can cancel only orders in the IN_PROGRESS status"))
                .thenAnswer(invocation -> getMockedMethodNotAllowedResponse("You can cancel only orders in the IN_PROGRESS status"));

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/" + API_VERSION + "/orders/2/cancel")
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Method not allowed"))
                .andExpect(jsonPath("$.detail").value("You can cancel only orders in the IN_PROGRESS status"));
    }

    @Test
    public void givenOrderInProgress_whenComplete_thenReturnCompletedOrderWithLinks() throws Exception {
        Order orderInProgress = new Order(1L, Status.IN_PROGRESS, "Meat");
        Order orderCompleted = new Order(1L, Status.COMPLETED, "Meat");

        when(orderService.isOrderInProgress(1L)).thenReturn(true);

        when(orderService.completeOrder(1L)).thenReturn(orderCompleted);

        // Mocked HAL representation for cancelledMockOrder
        when(orderEntityModelAssembler.toModel(orderCompleted))
                .thenReturn(EntityModel.of(orderCompleted)
                        .add(linkTo(methodOn(OrderController.class).getOrderById(orderCompleted.getId())).withSelfRel())
                        .add(linkTo(methodOn(OrderController.class).getOrders()).withRel("orders")));

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/" + API_VERSION + "/orders/1/complete")
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(orderInProgress.getId()))
                .andExpect(jsonPath("$.status").value(orderCompleted.getStatus().toString()))
                .andExpect(jsonPath("$.description").value(orderInProgress.getDescription()))
                .andExpect(jsonPath("$._links.self.href").value("/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$._links.orders.href").value("/" + API_VERSION + "/orders"));
    }

    @Test
    public void givenOrderCanceled_whenComplete_thenReturnMethodNotAllowed() throws Exception {
        Order orderCanceled = new Order(1L, Status.CANCELED, "Meat");
        when(orderService.isOrderInProgress(orderCanceled.getId())).thenReturn(false);

        when(orderEntityModelAssembler.getMethodNotAllowedResponse("You can cancel only orders in the IN_PROGRESS status"))
                .thenAnswer(invocation -> getMockedMethodNotAllowedResponse("You can cancel only orders in the IN_PROGRESS status"));

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/" + API_VERSION + "/orders/1/cancel")
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Method not allowed"))
                .andExpect(jsonPath("$.detail").value("You can cancel only orders in the IN_PROGRESS status"));
    }

}