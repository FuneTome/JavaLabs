public class InputException extends Exception {
  public InputException(String message) {
    super(message);
  }
  public InputException(String message, Double value) {
    super(message + value);
  }
}
