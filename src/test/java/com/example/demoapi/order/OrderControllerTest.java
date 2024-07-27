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

    private Order mockOrderInProgress;
    private Order mockOrderCompleted;
    private Order updatedMockOrderInProgress;
    private Order cancelledMockOrder;
    private static final MediaType HAL_JSON = new MediaType("application", "hal+json");

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        mockOrderInProgress = new Order(1L, Status.IN_PROGRESS, "Meat");
        mockOrderCompleted = new Order(2L, Status.COMPLETED, "Beans");
        updatedMockOrderInProgress = new Order(1L, Status.IN_PROGRESS, "Orange");
        cancelledMockOrder = new Order(1L, Status.CANCELED, "Meat");
        List<Order> mockOrders = List.of(mockOrderInProgress, mockOrderCompleted);

        // orderService methods
        when(orderService.getOrders()).thenReturn(mockOrders);
        when(orderService.getOrderById(1L)).thenReturn(mockOrderInProgress);
        when(orderService.getOrderById(2L)).thenReturn(mockOrderCompleted);
        when(orderService.saveOrder(mockOrderInProgress)).thenReturn(mockOrderInProgress);
        when(orderService.updateOrder(mockOrderInProgress, 2L)).thenReturn(mockOrderInProgress);

        // Mocked HAL representation for mockOrderInProgress
        when(orderEntityModelAssembler.toModel(mockOrderInProgress))
                .thenReturn(EntityModel.of(mockOrderInProgress)
                        .add(linkTo(methodOn(OrderController.class).getOrderById(mockOrderInProgress.getId())).withSelfRel())
                        .add(linkTo(methodOn(OrderController.class).getOrders()).withRel("orders"))
                        .add(linkTo(methodOn(OrderController.class).cancelOrder(mockOrderInProgress.getId())).withRel("cancel"))
                        .add(linkTo(methodOn(OrderController.class).completeOrder(mockOrderInProgress.getId())).withRel("complete")));

        // Mocked HAL representation for mockOrderCompleted
        when(orderEntityModelAssembler.toModel(mockOrderCompleted))
                .thenReturn(EntityModel.of(mockOrderCompleted)
                        .add(linkTo(methodOn(OrderController.class).getOrderById(mockOrderCompleted.getId())).withSelfRel())
                        .add(linkTo(methodOn(OrderController.class).getOrders()).withRel("orders")));

        // Mocked HAL representation for collection of orders
        when(orderEntityModelAssembler.toCollectionModel(mockOrders))
                .thenReturn(CollectionModel.of(List.of(
                        EntityModel.of(mockOrderInProgress)
                                .add(linkTo(methodOn(OrderController.class).getOrderById(mockOrderInProgress.getId())).withSelfRel())
                                .add(linkTo(methodOn(OrderController.class).getOrders()).withRel("orders"))
                                .add(linkTo(methodOn(OrderController.class).cancelOrder(mockOrderInProgress.getId())).withRel("cancel"))
                                .add(linkTo(methodOn(OrderController.class).completeOrder(mockOrderInProgress.getId())).withRel("complete")),
                        EntityModel.of(mockOrderCompleted)
                                .add(linkTo(methodOn(OrderController.class).getOrderById(mockOrderCompleted.getId())).withSelfRel())
                                .add(linkTo(methodOn(OrderController.class).getOrders()).withRel("orders"))
                )));
    }

    @Test
    public void givenOrdersExist_whenGetOrders_thenReturnOrderListWithLinks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/" + API_VERSION + "/orders")
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.orderList").exists())
                .andExpect(jsonPath("$._embedded.orderList[*].id").isNotEmpty())
                .andExpect(jsonPath("$._embedded.orderList[0].id").value(mockOrderInProgress.getId()))
                .andExpect(jsonPath("$._embedded.orderList[0].status").value(mockOrderInProgress.getStatus().toString()))
                .andExpect(jsonPath("$._embedded.orderList[0].description").value(mockOrderInProgress.getDescription()))
                .andExpect(jsonPath("$._embedded.orderList[0]._links.self.href").value("/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$._embedded.orderList[0]._links.orders.href").value("/" + API_VERSION + "/orders"))
                .andExpect(jsonPath("$._embedded.orderList[0]._links.cancel.href").value("/" + API_VERSION + "/orders/1/cancel"))
                .andExpect(jsonPath("$._embedded.orderList[0]._links.complete.href").value("/" + API_VERSION + "/orders/1/complete"))
                .andExpect(jsonPath("$._embedded.orderList[1].id").value(mockOrderCompleted.getId()))
                .andExpect(jsonPath("$._embedded.orderList[1].status").value(mockOrderCompleted.getStatus().toString()))
                .andExpect(jsonPath("$._embedded.orderList[1].description").value(mockOrderCompleted.getDescription()))
                .andExpect(jsonPath("$._embedded.orderList[1]._links.self.href").value("/" + API_VERSION + "/orders/2"))
                .andExpect(jsonPath("$._embedded.orderList[1]._links.orders.href").value("/" + API_VERSION + "/orders"));
    }

    @Test
    public void givenOrderExists_whenGetOrderById_thenReturnOrderWithLinks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/" + API_VERSION + "/orders/1")
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(mockOrderInProgress.getId()))
                .andExpect(jsonPath("$.status").value(mockOrderInProgress.getStatus().toString()))
                .andExpect(jsonPath("$.description").value(mockOrderInProgress.getDescription()))
                .andExpect(jsonPath("$._links.self.href").value("/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$._links.orders.href").value("/" + API_VERSION + "/orders"))
                .andExpect(jsonPath("$._links.cancel.href").value("/" + API_VERSION + "/orders/1/cancel"))
                .andExpect(jsonPath("$._links.complete.href").value("/" + API_VERSION + "/orders/1/complete"));
    }

    @Test
    public void givenNewOrder_whenPostOrder_thenReturnSavedOrderWithLinks() throws Exception {
        String jsonOrder = objectMapper.writeValueAsString(mockOrderInProgress);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/" + API_VERSION + "/orders")
                        .contentType(HAL_JSON).content(jsonOrder)
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(mockOrderInProgress.getId()))
                .andExpect(jsonPath("$.status").value(mockOrderInProgress.getStatus().toString()))
                .andExpect(jsonPath("$.description").value(mockOrderInProgress.getDescription()))
                .andExpect(jsonPath("$._links.self.href").value("/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$._links.orders.href").value("/" + API_VERSION + "/orders"))
                .andExpect(jsonPath("$._links.cancel.href").value("/" + API_VERSION + "/orders/1/cancel"))
                .andExpect(jsonPath("$._links.complete.href").value("/" + API_VERSION + "/orders/1/complete"));
    }

    @Test
    public void givenOrderExists_whenPutOrderWithNewParameters_thenReturnUpdatedOrderWithLinks() throws Exception {
        String jsonOrder = objectMapper.writeValueAsString(mockOrderInProgress);
        mockMvc.perform(MockMvcRequestBuilders
                        .put(("/" + API_VERSION + "/orders/2"), mockOrderInProgress)
                        .content(jsonOrder)
                        .contentType(HAL_JSON)
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(mockOrderInProgress.getId()))
                .andExpect(jsonPath("$.status").value(mockOrderInProgress.getStatus().toString()))
                .andExpect(jsonPath("$.description").value(mockOrderInProgress.getDescription()))
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
        when(orderService.cancelOrder(1L)).thenReturn(cancelledMockOrder);
        when(orderService.isOrderInProgress(1L)).thenReturn(true);

        // Mocked HAL representation for cancelledMockOrder
        when(orderEntityModelAssembler.toModel(cancelledMockOrder))
                .thenReturn(EntityModel.of(cancelledMockOrder)
                        .add(linkTo(methodOn(OrderController.class).getOrderById(cancelledMockOrder.getId())).withSelfRel())
                        .add(linkTo(methodOn(OrderController.class).getOrders()).withRel("orders")));

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/" + API_VERSION + "/orders/1/cancel")
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(mockOrderInProgress.getId()))
                .andExpect(jsonPath("$.status").value(cancelledMockOrder.getStatus().toString()))
                .andExpect(jsonPath("$.description").value(mockOrderInProgress.getDescription()))
                .andExpect(jsonPath("$._links.self.href").value("/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$._links.orders.href").value("/" + API_VERSION + "/orders"));
    }

    @Test
    public void givenOrderCompletedOrder_whenCancel_thenReturnMethodNotAllowed() throws Exception {
        when(orderService.isOrderInProgress(2L)).thenReturn(false);

        ResponseEntity<?> responseEntity = ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(Problem.create()
                        .withTitle("Method not allowed")
                        .withDetail("You can cancel only orders in the IN_PROGRESS status"));
        when(orderEntityModelAssembler.getMethodNotAllowedResponse("You can cancel only orders in the IN_PROGRESS status"))
                .thenAnswer(invocation -> responseEntity);

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
        when(orderService.isOrderInProgress(1L)).thenReturn(true);
        Order completedMockedOrder = new Order(1L, Status.COMPLETED, "Meat");
        when(orderService.completeOrder(1L)).thenReturn(completedMockedOrder);

        // Mocked HAL representation for cancelledMockOrder
        when(orderEntityModelAssembler.toModel(completedMockedOrder))
                .thenReturn(EntityModel.of(completedMockedOrder)
                        .add(linkTo(methodOn(OrderController.class).getOrderById(completedMockedOrder.getId())).withSelfRel())
                        .add(linkTo(methodOn(OrderController.class).getOrders()).withRel("orders")));

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/" + API_VERSION + "/orders/1/complete")
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(mockOrderInProgress.getId()))
                .andExpect(jsonPath("$.status").value(mockOrderCompleted.getStatus().toString()))
                .andExpect(jsonPath("$.description").value(mockOrderInProgress.getDescription()))
                .andExpect(jsonPath("$._links.self.href").value("/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$._links.orders.href").value("/" + API_VERSION + "/orders"));
    }

    @Test
    public void givenOrderCanceled_whenComplete_thenReturnMethodNotAllowed() throws Exception {
        Order canceledOrder = new Order(1L, Status.CANCELED, "Meat");
        when(orderService.isOrderInProgress(canceledOrder.getId())).thenReturn(false);

        ResponseEntity<?> responseEntity = ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(Problem.create()
                        .withTitle("Method not allowed")
                        .withDetail("You can cancel only orders in the IN_PROGRESS status"));
        when(orderEntityModelAssembler.getMethodNotAllowedResponse("You can cancel only orders in the IN_PROGRESS status"))
                .thenAnswer(invocation -> responseEntity);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/" + API_VERSION + "/orders/1/cancel")
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Method not allowed"))
                .andExpect(jsonPath("$.detail").value("You can cancel only orders in the IN_PROGRESS status"));
    }


}