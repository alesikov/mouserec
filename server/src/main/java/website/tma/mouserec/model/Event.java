package website.tma.mouserec.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Table
@Builder
public class Event {

    @Id
    private UUID id;

    private UUID sessionId;

    private int x;

    private int y;

    private long delay;

}
