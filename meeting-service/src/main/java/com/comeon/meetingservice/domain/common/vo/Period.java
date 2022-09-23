package com.comeon.meetingservice.domain.common.vo;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Period {

    private LocalDate startDate;
    private LocalDate endDate;

    private Period(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static Period createPeriod(LocalDate startDate, LocalDate endDate) {
        if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
            throw new CustomException("기간의 시작일 혹은 종료일이 없습니다.", ErrorCode.PERIOD_NOT_EXIST);
        } else if (startDate.isAfter(endDate)){
            throw new CustomException("기간의 시작일보다 종료일이 앞섭니다.", ErrorCode.INVALID_PERIOD);
        } else {
            return new Period(startDate, endDate);
        }
    }

    public boolean isWithinPeriod(LocalDate date) {
        return startDate.compareTo(date) <= 0 && endDate.compareTo(date) >= 0;
    }
}
