// package com.exam.library_management.service;

// import com.exam.library_management.entity.Book;
// import com.exam.library_management.enums.BookStatus;
// import com.exam.library_management.exception.BadRequestException;
// import com.exam.library_management.exception.ResourceNotFoundException;
// import com.exam.library_management.repository.BookRepository;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;

// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import org.springframework.dao.DataIntegrityViolationException;

// import java.util.List;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class BookServiceTest {

//     @Mock
//     private BookRepository bookRepository;

//     @InjectMocks
//     private BookService bookService;

//     private Book book;

//     @BeforeEach
//     void setUp() {
//         book = new Book();
//         book.setId(1L);
//         book.setTitle("Spring Boot");
//         book.setAuthor("Pawan");
//         book.setStatus(BookStatus.AVAILABLE);
//     }

//     // =====================================================
//     // ADD BOOK
//     // =====================================================

//     @Test
//     void addBook_success() {

//         when(bookRepository.save(any(Book.class))).thenReturn(book);

//         Book result = bookService.addBook(book);

//         assertNotNull(result);
//         assertEquals(BookStatus.AVAILABLE, result.getStatus());
//         verify(bookRepository).save(book);
//     }

//     @Test
//     void addBook_duplicateCode_shouldThrowBadRequest() {

//         when(bookRepository.save(any(Book.class)))
//                 .thenThrow(new DataIntegrityViolationException("Duplicate"));

//         BadRequestException exception = assertThrows(
//                 BadRequestException.class,
//                 () -> bookService.addBook(book)
//         );

//         assertEquals("Book with this code already exists", exception.getMessage());
//     }

//     // =====================================================
//     // UPDATE BOOK
//     // =====================================================

//     @Test
//     void updateBook_success_allFields() {

//         Book updated = new Book();
//         updated.setTitle("Updated Title");
//         updated.setAuthor("Updated Author");
//         updated.setStatus(BookStatus.BORROWED);

//         when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
//         when(bookRepository.save(any(Book.class))).thenReturn(book);

//         Book result = bookService.updateBook(1L, updated);

//         assertEquals("Updated Title", result.getTitle());
//         assertEquals("Updated Author", result.getAuthor());
//         assertEquals(BookStatus.BORROWED, result.getStatus());

//         verify(bookRepository).save(book);
//     }

//     @Test
//     void updateBook_partialUpdate_onlyTitle() {

//         Book updated = new Book();
//         updated.setTitle("Only Title Updated");

//         when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
//         when(bookRepository.save(any(Book.class))).thenReturn(book);

//         Book result = bookService.updateBook(1L, updated);

//         assertEquals("Only Title Updated", result.getTitle());
//         assertEquals("Pawan", result.getAuthor()); // unchanged
//     }

//     @Test
//     void updateBook_notFound_shouldThrowException() {

//         when(bookRepository.findById(1L)).thenReturn(Optional.empty());

//         assertThrows(ResourceNotFoundException.class,
//                 () -> bookService.updateBook(1L, new Book()));
//     }

//     // =====================================================
//     // DELETE BOOK
//     // =====================================================

//     @Test
//     void deleteBook_success() {

//         when(bookRepository.existsById(1L)).thenReturn(true);

//         bookService.deleteBook(1L);

//         verify(bookRepository).deleteById(1L);
//     }

//     @Test
//     void deleteBook_notFound_shouldThrowException() {

//         when(bookRepository.existsById(1L)).thenReturn(false);

//         assertThrows(ResourceNotFoundException.class,
//                 () -> bookService.deleteBook(1L));
//     }

//     // =====================================================
//     // GET ALL BOOKS
//     // =====================================================

//     @Test
//     void getAllBooks_success() {

//         when(bookRepository.findAll()).thenReturn(List.of(book));

//         List<Book> result = bookService.getAllBooks();

//         assertEquals(1, result.size());
//         verify(bookRepository).findAll();
//     }

//     // =====================================================
//     // GET AVAILABLE BOOKS
//     // =====================================================

//     @Test
//     void getAvailableBooks_success() {

//         when(bookRepository.findByStatus(BookStatus.AVAILABLE))
//                 .thenReturn(List.of(book));

//         List<Book> result = bookService.getAvailableBooks();

//         assertEquals(1, result.size());
//         verify(bookRepository).findByStatus(BookStatus.AVAILABLE);
//     }
// }

package com.exam.library_management.service;

import com.exam.library_management.entity.Book;
import com.exam.library_management.enums.BookStatus;
import com.exam.library_management.exception.BadRequestException;
import com.exam.library_management.exception.ResourceNotFoundException;
import com.exam.library_management.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    /* ==========================
       ADD BOOK
       ========================== */

    @Test
    void addBook_ShouldSetStatusToAvailable_AndSave() {
        Book book = new Book();
        book.setTitle("Test");

        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.addBook(book);

        assertEquals(BookStatus.AVAILABLE, book.getStatus());
        verify(bookRepository).save(book);
    }

    @Test
    void addBook_ShouldThrowBadRequest_WhenDuplicateCode() {
        Book book = new Book();

        when(bookRepository.save(any(Book.class)))
                .thenThrow(DataIntegrityViolationException.class);

        assertThrows(BadRequestException.class,
                () -> bookService.addBook(book));
    }

    /* ==========================
       UPDATE BOOK
       ========================== */

    @Test
    void updateBook_ShouldUpdateAllFields_WhenAllProvided() {
        Book existing = new Book();
        existing.setId(1L);
        existing.setTitle("Old");
        existing.setAuthor("OldAuthor");
        existing.setStatus(BookStatus.AVAILABLE);

        Book updated = new Book();
        updated.setTitle("New");
        updated.setAuthor("NewAuthor");
        updated.setStatus(BookStatus.BORROWED);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenReturn(existing);

        Book result = bookService.updateBook(1L, updated);

        assertEquals("New", result.getTitle());
        assertEquals("NewAuthor", result.getAuthor());
        assertEquals(BookStatus.BORROWED, result.getStatus());
    }

    @Test
    void updateBook_ShouldUpdateOnlyTitle_WhenOnlyTitleProvided() {
        Book existing = new Book();
        existing.setId(1L);
        existing.setTitle("Old");
        existing.setAuthor("Author");
        existing.setStatus(BookStatus.AVAILABLE);

        Book updated = new Book();
        updated.setTitle("UpdatedTitle");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenReturn(existing);

        Book result = bookService.updateBook(1L, updated);

        assertEquals("UpdatedTitle", result.getTitle());
        assertEquals("Author", result.getAuthor()); // unchanged
        assertEquals(BookStatus.AVAILABLE, result.getStatus()); // unchanged
    }

    @Test
    void updateBook_ShouldNotChangeAnything_WhenAllFieldsNull() {
        Book existing = new Book();
        existing.setId(1L);
        existing.setTitle("Old");
        existing.setAuthor("OldAuthor");
        existing.setStatus(BookStatus.AVAILABLE);

        Book updated = new Book(); // all null

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenReturn(existing);

        Book result = bookService.updateBook(1L, updated);

        assertEquals("Old", result.getTitle());
        assertEquals("OldAuthor", result.getAuthor());
        assertEquals(BookStatus.AVAILABLE, result.getStatus());
    }

    @Test
    void updateBook_ShouldThrowException_WhenBookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class,
                        () -> bookService.updateBook(1L, new Book()));

        assertEquals("Book not found", exception.getMessage());
    }


    /* ==========================
       DELETE BOOK
       ========================== */

    @Test
    void deleteBook_ShouldDelete_WhenBookExists() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        bookService.deleteBook(1L);

        verify(bookRepository).deleteById(1L);
    }

    @Test
    void deleteBook_ShouldThrowException_WhenNotFound() {
        when(bookRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> bookService.deleteBook(1L));
    }

    /* ==========================
       GET ALL BOOKS
       ========================== */

    @Test
    void getAllBooks_ShouldReturnList() {
        List<Book> books = Arrays.asList(new Book(), new Book());
        when(bookRepository.findAll()).thenReturn(books);

        List<Book> result = bookService.getAllBooks();

        assertEquals(2, result.size());
    }

    /* ==========================
       GET AVAILABLE BOOKS
       ========================== */

    @Test
    void getAvailableBooks_ShouldReturnOnlyAvailableBooks() {
        List<Book> books = Arrays.asList(new Book(), new Book());
        when(bookRepository.findByStatus(BookStatus.AVAILABLE))
                .thenReturn(books);

        List<Book> result = bookService.getAvailableBooks();

        assertEquals(2, result.size());
        verify(bookRepository).findByStatus(BookStatus.AVAILABLE);
    }

    @Test
    void updateBook_ShouldUpdateOnlyAuthor() {
        Book existing = new Book();
        existing.setId(1L);
        existing.setTitle("OldTitle");
        existing.setAuthor("OldAuthor");
        existing.setStatus(BookStatus.AVAILABLE);

        Book updated = new Book();
        updated.setAuthor("NewAuthor");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenReturn(existing);

        Book result = bookService.updateBook(1L, updated);

        assertEquals("OldTitle", result.getTitle());
        assertEquals("NewAuthor", result.getAuthor());
        assertEquals(BookStatus.AVAILABLE, result.getStatus());
    }

    @Test
    void updateBook_ShouldUpdateOnlyStatus() {
        Book existing = new Book();
        existing.setId(1L);
        existing.setTitle("OldTitle");
        existing.setAuthor("OldAuthor");
        existing.setStatus(BookStatus.AVAILABLE);

        Book updated = new Book();
        updated.setStatus(BookStatus.BORROWED);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenReturn(existing);

        Book result = bookService.updateBook(1L, updated);

        assertEquals("OldTitle", result.getTitle());
        assertEquals("OldAuthor", result.getAuthor());
        assertEquals(BookStatus.BORROWED, result.getStatus());
    }

    @Test
    void updateBook_ShouldUpdateTitleAndAuthorOnly() {
        Book existing = new Book();
        existing.setId(1L);
        existing.setTitle("Old");
        existing.setAuthor("OldAuthor");
        existing.setStatus(BookStatus.AVAILABLE);

        Book updated = new Book();
        updated.setTitle("NewTitle");
        updated.setAuthor("NewAuthor");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenReturn(existing);

        Book result = bookService.updateBook(1L, updated);

        assertEquals("NewTitle", result.getTitle());
        assertEquals("NewAuthor", result.getAuthor());
        assertEquals(BookStatus.AVAILABLE, result.getStatus()); // unchanged
    }

    @Test
    void updateBook_ShouldNotUpdateStatus_WhenStatusIsNullButOthersPresent() {
    
        Book existing = new Book();
        existing.setId(1L);
        existing.setTitle("OldTitle");
        existing.setAuthor("OldAuthor");
        existing.setStatus(BookStatus.AVAILABLE);
    
        Book updated = new Book();
        updated.setTitle("NewTitle");
        updated.setAuthor("NewAuthor");
        updated.setStatus(null); // explicit null
    
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenReturn(existing);
    
        Book result = bookService.updateBook(1L, updated);
    
        assertEquals("NewTitle", result.getTitle());
        assertEquals("NewAuthor", result.getAuthor());
        assertEquals(BookStatus.AVAILABLE, result.getStatus()); // unchanged
    
        verify(bookRepository).save(existing);
    }

}
