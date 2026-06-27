package com.fitnesscenter.dto;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ClientFilterRequest {
    private String name;
    private String phone;
    private String email;
    private String searchQuery;

    private String sortField = "lastname";
    private String sortOrder = "asc";

    @Override
    public String toString() {
        return "ClientFilterRequest{searchQuery='" + searchQuery + "', sortField='" + sortField + "', sortOrder='" + sortOrder + "'}";
    }

}
