package org.example.aioschedulingservice.infrastructure.configs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "scheduling")
@Component
@Getter
@Setter
@NoArgsConstructor
public class SchedulingProperties {
    private Rules rules;
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Rules {
        private int periodWeeks;
        private DayOff dayOff;
        private boolean avoidMorningAfterEvening;
        private boolean balanceShifts;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class DayOff {
            private boolean enabled;
            private int daysPerWeek;
        }
    }
}
