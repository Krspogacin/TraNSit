package org.mad.transit.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Zone implements Serializable {
    private static final long serialVersionUID = 1146407107264021796L;
    private Long id;

    @EqualsAndHashCode.Include
    private String name;
}
