package website.tma.mouserec.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Table
@Builder
public class Session {

    @Id
    private UUID id;

    private Instant startedAt;

    private Instant stoppedAt;

}
