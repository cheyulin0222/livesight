package com.arplanets.corexrapi.livesight.model;

import com.arplanets.corexrapi.livesight.log.ErrorContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseContext {

    private OrderContext order;

    private ErrorContext errorContext;
}
