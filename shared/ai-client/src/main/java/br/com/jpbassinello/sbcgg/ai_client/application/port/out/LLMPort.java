package br.com.jpbassinello.sbcgg.ai_client.application.port.out;

import jakarta.annotation.Nullable;

public interface LLMPort {

  <T> Response<T> fetchResponses(String input, Model model, Class<T> responseClass);

  /**
   * The quality/cost tier for a call, resolved to a concrete model by the adapter. Pick by how
   * much a bad answer costs and how often the call runs, not by how hard the task feels:
   *
   * <ul>
   *   <li>{@code PREMIUM} — rare, high-stakes calls whose output grounds later work. A bad answer
   *       is expensive and often invisible until much later.</li>
   *   <li>{@code STANDARD} — user-facing writing at moderate volume.</li>
   *   <li>{@code LIGHTWEIGHT} — high-volume extraction and classification.</li>
   * </ul>
   */
  enum Model {
    PREMIUM,
    STANDARD,
    LIGHTWEIGHT
  }

  record Response<T>(
      @Nullable T response,
      @Nullable String errorMessage,
      @Nullable Exception exception
  ) {}
}
