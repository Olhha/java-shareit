package ru.practicum.shareit.booking.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest
@AutoConfigureJsonTesters
class BookingJsonTest {
    @Autowired
    private JacksonTester<BookingRequestDto> json;

    @SneakyThrows
    @Test
    void bookingRequestDto_bookerId_JsonIgnore() {
        BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                .start(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS))
                .end(LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS))
                .itemId(1L)
                .bookerId(2L)
                .build();

        JsonContent<BookingRequestDto> result = json.write(bookingRequestDto);

        assertThat(result).hasEmptyJsonPathValue("$.bookerId");
        assertThat(result).extractingJsonPathValue("$.itemId")
                .isEqualTo(1);
        assertThat(result).extractingJsonPathValue("$.start")
                .isEqualTo(bookingRequestDto.getStart().toString());
        assertThat(result).extractingJsonPathValue("$.end")
                .isEqualTo(bookingRequestDto.getEnd().toString());
    }
}