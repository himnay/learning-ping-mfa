package com.org.learningpingmfa.dto;

public record UserRef(Id user) {
    public record Id(String id) {
    }
}
