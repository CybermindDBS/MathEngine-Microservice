package com.cdev.mathengineclient.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("calculations")
@Data
@NoArgsConstructor()
@ToString
public class Calculation {
    @Id
    private Long id;
    private String userId;
    private String uuid;
    private String mainExpr;
    private String variables;
    private String functions;
    private String result;
    private String logs;
    private String timeElapsed;
    private LocalDateTime dateTime;
    @Column("input_str")
    private String input;
    @Column("output_str")
    private String output;
    private String shortInputOutput;

    public Calculation(String userId, String UUID, String mainExpr, String variables, String functions, String result, String logs, String timeElapsed) {
        this.userId = userId;
        this.uuid = UUID;
        this.mainExpr = mainExpr;
        this.variables = variables;
        this.functions = functions;
        this.result = result;
        this.logs = logs;
        this.timeElapsed = timeElapsed;
        this.dateTime = LocalDateTime.now();
        this.input = mainExpr + "\n" + variables + "\n" + functions;
        this.output = result + "\n" + logs + "\n" + timeElapsed;
        this.shortInputOutput = mainExpr + "\n" + (!result.contains("Error") ? result : "Error.");
    }

    public void setDateTime() {
        this.dateTime = LocalDateTime.now();
    }
}
