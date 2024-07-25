package com.example.demoapi.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static com.example.demoapi.DemoapiApplication.API_VERSION;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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

    private Order mockOrder1;
    private Order mockOrder2;
    private static final MediaType HAL_JSON = new MediaType("application", "hal+json");

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        mockOrder1 = new Order(1L, Status.IN_PROGRESS, "Meat");
        mockOrder2 = new Order(2L, Status.COMPLETED, "Beans");
        List<Order> mockOrders = List.of(mockOrder1, mockOrder2);

        when(orderService.getOrders()).thenReturn(mockOrders);
        when(orderService.getOrderById(1L)).thenReturn(mockOrder1);
        when(orderService.getOrderById(2L)).thenReturn(mockOrder2);

        // Mocked HAL representation for mockOrder1
        when(orderEntityModelAssembler.toModel(mockOrder1))
                .thenReturn(EntityModel.of(mockOrder1)
                        .add(linkTo(methodOn(OrderController.class).getOrderById(mockOrder1.getId())).withSelfRel())
                        .add(linkTo(methodOn(OrderController.class).getOrders()).withRel("orders"))
                        .add(linkTo(methodOn(OrderController.class).cancelOrder(mockOrder1.getId())).withRel("cancel"))
                        .add(linkTo(methodOn(OrderController.class).completeOrder(mockOrder1.getId())).withRel("complete")));

        // Mocked HAL representation for mockOrder2
        when(orderEntityModelAssembler.toModel(mockOrder2))
                .thenReturn(EntityModel.of(mockOrder2)
                        .add(linkTo(methodOn(OrderController.class).getOrderById(mockOrder2.getId())).withSelfRel())
                        .add(linkTo(methodOn(OrderController.class).getOrders()).withRel("orders")));

        // Mocked HAL representation for collection of orders
        when(orderEntityModelAssembler.toCollectionModel(mockOrders))
                .thenReturn(CollectionModel.of(List.of(
                        EntityModel.of(mockOrder1)
                                .add(linkTo(methodOn(OrderController.class).getOrderById(mockOrder1.getId())).withSelfRel())
                                .add(linkTo(methodOn(OrderController.class).getOrders()).withRel("orders"))
                                .add(linkTo(methodOn(OrderController.class).cancelOrder(mockOrder1.getId())).withRel("cancel"))
                                .add(linkTo(methodOn(OrderController.class).completeOrder(mockOrder1.getId())).withRel("complete")),
                        EntityModel.of(mockOrder2)
                                .add(linkTo(methodOn(OrderController.class).getOrderById(mockOrder2.getId())).withSelfRel())
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
                .andExpect(jsonPath("$._embedded.orderList[0].id").value(mockOrder1.getId()))
                .andExpect(jsonPath("$._embedded.orderList[0].status").value(mockOrder1.getStatus().toString()))
                .andExpect(jsonPath("$._embedded.orderList[0].description").value(mockOrder1.getDescription()))
                .andExpect(jsonPath("$._embedded.orderList[0]._links.self.href").value("/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$._embedded.orderList[0]._links.cancel.href").value("/" + API_VERSION + "/orders/1/cancel"))
                .andExpect(jsonPath("$._embedded.orderList[0]._links.complete.href").value("/" + API_VERSION + "/orders/1/complete"))
                .andExpect(jsonPath("$._embedded.orderList[1].id").value(mockOrder2.getId()))
                .andExpect(jsonPath("$._embedded.orderList[1].status").value(mockOrder2.getStatus().toString()))
                .andExpect(jsonPath("$._embedded.orderList[1].description").value(mockOrder2.getDescription()))
                .andExpect(jsonPath("$._embedded.orderList[1]._links.self.href").value("/" + API_VERSION + "/orders/2"))
                .andExpect(jsonPath("$._embedded.orderList[1]._links.orders.href").value("/" + API_VERSION + "/orders"));
    }

    @Test
    public void givenOrder1Exist_whenGetOrderById_thenReturnOrderWithLinks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/" + API_VERSION + "/orders/1")
                        .accept(HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(mockOrder1.getId()))
                .andExpect(jsonPath("$.status").value(mockOrder1.getStatus().toString()))
                .andExpect(jsonPath("$.description").value(mockOrder1.getDescription()))
                .andExpect(jsonPath("$._links.self.href").value("/" + API_VERSION + "/orders/1"))
                .andExpect(jsonPath("$._links.cancel.href").value("/" + API_VERSION + "/orders/1/cancel"))
                .andExpect(jsonPath("$._links.complete.href").value("/" + API_VERSION + "/orders/1/complete"));
    }

}