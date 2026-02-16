package com.exam.library_management.entity;

import com.exam.library_management.enums.BookStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "books",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "book_code")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_code", unique = true, nullable = false)
    private String bookCode;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status = BookStatus.AVAILABLE;
}
