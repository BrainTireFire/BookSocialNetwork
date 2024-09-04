package com.tengo.book.book;

import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {

    public static Specification<Book> withOwnerId(Integer ownerId) {
        return (root, query, criterialBuilder) -> criterialBuilder.equal(root.get("owner").get("id"), ownerId);
    }
}
