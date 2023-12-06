package Code;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class Admin extends Person {
	Calendar cal = Calendar.getInstance();

	public Admin(int id, String password, String name, String surname) {
		super(id, password, name, surname);
	}

	public Admin() {
		// TODO Auto-generated constructor stub
	}

	public void addBook(Book book) {
			BookDatabaseObject bookdao = new BookDatabaseObject();	
			Book bk =  Library.getBookByISBN(book.getISBN());	
			if(bk==null){				
			 book.setQuantity(1);
			 Library.getBooks().add(book);
			 bookdao.add(book);
			 }
			else{
			 bk.setQuantity(bk.getQuantity()+1);
			 bookdao.update(bk);
			 }			
	}
	
	public List<User> listFine() {
		List<User> users = Library.getUsers();
		List<User> usersFine = new ArrayList<User>();
		for(User user: users) {
			if(user.isHasFine()) {
				usersFine.add(user);
			}
		}
		return usersFine;
	}
	
	public boolean deleteBookByISBN(int ISBN) {
		BookDatabaseObject bookdao = new BookDatabaseObject();	
		Book bk =  Library.getBookByISBN(ISBN);	
		if(bk==null){				
		 return false;
		 }
		else{
			if(bk.getQuantity() == 1) {
				Library.getBooks().remove(bk);
				bookdao.delete(bk.getISBN());
			}
			else {
				bk.setQuantity(bk.getQuantity()-1);
				bookdao.update(bk);
			}
		 }
		return true;
	}

	public boolean giveBook(int ISBN, int ID) {
	    BookDatabaseObject bookdao = new BookDatabaseObject();
	    Book book = Library.getBookByISBN(ISBN);
	    PersonDAO_Imp persondao = new PersonDAO_Imp();
	    User user = Library.getUserByID(ID);

	    try {
	        if (user.getBookReceived() != null && (user.getBookReceived().contains(book) || user.isHasFine())) {
	            System.out.println("User already has the book or has a fine.");
	            return false;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    if (book.getQuantity() == 0) {
	        System.out.println("This book is not available");
	        return false;
	    } else {
	        try {
	            if (book.getQuantity() == 1) {
	                book.setQuantity(0);
	                bookdao.update(book);
	            } else {
	                book.setQuantity(book.getQuantity() - 1);
	                bookdao.update(book);
	            }

	            user.getBookReceived().add(book);
	            user.getDeadlines().add(java.sql.Date.valueOf(Library.getDate().plusDays(15)));
	            persondao.update(user);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        }
	    }

	    return true;
	}


	public boolean bookReturn(int ISBN, int ID) {
	    try {
	        BookDatabaseObject bookdao = new BookDatabaseObject();
	        PersonDAO_Imp persondao = new PersonDAO_Imp();

	        User user = Library.getUserByID(ID);
	        Book book = user.getBookByISBN(ISBN);

	        if (user.getBookReceived().contains(book)) {
	            book.setQuantity(book.getQuantity() + 1);

	            // Use Iterator to safely remove elements from the list
	            Iterator<Book> iterator = user.getBookReceived().iterator();
	            while (iterator.hasNext()) {
	                Book receivedBook = iterator.next();
	                if (receivedBook.equals(book)) {
	                    iterator.remove();
	                    break;
	                }
	            }

	            // Remove deadline if found
	            int bookIndex = user.getBookReceived().indexOf(book);
	            if (bookIndex != -1 && bookIndex < user.getDeadlines().size()) {
	                user.getDeadlines().remove(bookIndex);
	            }

	            user.getBookReadBefore().add(book);
	            user.setHasFine(false);
	            Library.checkFines();
	            bookdao.update(book);
	            persondao.update(user);
	            return true;
	        } else {
	            return false;
	        }
	    } catch (Exception e) {
	        // Log or handle the exception
	        return false;
	    }
	}


	public List<Book> searchBook(String string) {
		List<Book> searchingBooks = new ArrayList<Book>();
		for (int i = 0; i < Library.getBooks().size(); i++) {
			if (Library.getBooks().get(i).getAuthor().trim().contains(string)
					|| Library.getBooks().get(i).getName().trim().contains(string)) {
				searchingBooks.add(Library.getBooks().get(i));
			}
		}
		BookDatabaseObject book = new BookDatabaseObject();
		searchingBooks = book.Search(string, true);
		return searchingBooks;
	}

	public void listBook() {
		System.out.println(
				"Book name: " + "-" + "\t\t" + "Author: " + "-" + "\t\t" + "ISBN: " + "-" + "\t\t" + "Quantity: ");
		for (int i = 0; i < Library.getBooks().size(); i++) {
			System.out.println(Library.getBooks().get(i).getName() + "-" + "\t\t"
					+ Library.getBooks().get(i).getAuthor() + "-" + "\t\t" + Library.getBooks().get(i).getISBN() + "-"
					+ "\t\t" + Library.getBooks().get(i).getQuantity());
		}
	}
}
