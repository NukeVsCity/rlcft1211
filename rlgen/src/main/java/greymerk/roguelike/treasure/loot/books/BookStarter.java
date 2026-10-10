package greymerk.roguelike.treasure.loot.books;

import greymerk.roguelike.treasure.loot.Book;

public class BookStarter extends Book {
   public BookStarter() {
      super("greymerk", "Roguelike Dungeons");
      this.addPage(
         "Roguelike Dungeons\nThank you for playing <3\n\n    - Credits - \nAuthor: @Greymerk\nBits: Drainedsoul\nIdeas: Eniko @enichan\nContributions: Fnar\n"
      );
   }
}
