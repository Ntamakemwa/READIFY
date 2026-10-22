export interface Author {
  id: number;
  fullName: string;
  biography?: string;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
}

export interface Book {
  id: number;
  title: string;
  isbn: string;
  description: string;
  publishedYear: number;
  language: string;
  publisher: string;
  author: Author;
  category: Category;
  availableCopies?: number;
}

export interface LoginResponse {
  token: string;
  user?: Record<string, unknown>;
}

export interface Loan {
  id: number;
  status: string;
  book?: Book;
  borrowedDate?: string;
  loanDate?: string;
  dueDate?: string;
  returnDate?: string;
  returnedDate?: string;
}
