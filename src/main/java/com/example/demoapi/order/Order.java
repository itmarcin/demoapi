package com.example.demoapi.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document("orders")
class Order {

    @Id
    private Long id;
    private Status status;
    private String description;

    public Order() {
    }

    public Order(Long id, Status status, String description) {
        this.id = id;
        this.status = status;
        this.description = description;
    }

    public Order(String description, Status status) {
        this.description = description;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id) && Objects.equals(description, order.description) && status == order.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, status);
    }

    @Override
    public String toString() {
        return String.format(
                "Order[id=%s, status=%s, description=%s]",
                id, status, description);
    }

    public Long getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnore
    public boolean isInProgress() {
        return this.status.equals(Status.IN_PROGRESS);
    }
}
