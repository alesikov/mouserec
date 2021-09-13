package website.tma.mouserec.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table
@Builder
@Getter
public class Session {

    @Id
    private UUID id;

    private Instant startedAt;

    private Instant stoppedAt;

}
