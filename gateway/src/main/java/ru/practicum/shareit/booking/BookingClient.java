package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;

import javax.validation.ValidationException;
import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getBookings(
            String path, long userId, String state, Integer from, Integer size) {
        BookingState stateParsed = parseBookingState(state);

        Map<String, Object> parameters = Map.of(
                "state", stateParsed.name(),
                "from", from,
                "size", size
        );
        return get(path + "?state={state}&from={from}&size={size}", userId, parameters);
    }

    private BookingState parseBookingState(String state) {

        for (BookingState s : BookingState.values()) {
            if (s.name().equalsIgnoreCase(state)) {
                return s;
            }
        }
        throw new ValidationException("Unknown state: " + state);
    }


    public ResponseEntity<Object> addBooking(long userId, BookingRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> getBookingById(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> approveBooking(long userID, long bookingId, Boolean approved) {
        return patch("/" + bookingId + "?approved=" + approved, userID);
    }
}
