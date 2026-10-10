package com.clayfin.training.minirib.dto.request;

import com.clayfin.training.minirib.enums.DownloadType;
import com.clayfin.training.minirib.enums.TransferType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PaymentReportRequest(

        @NotNull(message = "download_type is required")
        @JsonProperty("download_type")
        DownloadType downloadType,

        @JsonProperty("ulp_id")
        Long ulpId,

        @NotNull(message = "transfer_type is required")
        @JsonProperty("transfer_type")
        TransferType transferType,

        @NotNull(message = "f_date is required")
        @JsonProperty("f_date")
        LocalDate fromDate,

        @NotNull(message = "t_date is required")
        @JsonProperty("t_date")
        LocalDate toDate
) {
    @AssertTrue(message = "f_date must be on or before t_date")
    public boolean isDateRangeValid() {
        return fromDate == null || toDate == null || !fromDate.isAfter(toDate);
    }
}