package com.romin.user.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import com.romin.task.entity.Task;
import com.romin.user.enums.Position;
import com.romin.user.enums.Role;
import com.romin.user.enums.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.ForeignKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "users",
    uniqueConstraints ={ 
        @UniqueConstraint(
            name = "uk_email",
            columnNames = {"user_email"}
        ),
        @UniqueConstraint(
            name = "uk_identity",
            columnNames = {"user_identity_number"}
        ),
        @UniqueConstraint(
            name = "uk_phone_number",
            columnNames = {"phone_number"}
        )                  
    }                 
)
public class User {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "user_id_generator"
    )
    @SequenceGenerator(
        name = "user_id_generator",
        sequenceName = "user_generator",
        initialValue = 1,
        allocationSize = 1
    )
    private Long id;

    @Column(
        name = "user_identity_number",
        nullable = false,
        length = 50
    )
    private String companyId;

    @Column(
        name = "name",
        nullable = false,
        length = 100
    )
    private String fullName;

    @Column(
        name = "phone_number",
        nullable = false,
        length = 20
    )
    private String phoneNumber;

    @Column(
        name = "role",
        nullable = false,
        length = 30
    )
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Role role;

    @Column(
        name = "position",
        nullable = false,
        length = 50
    )
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Position position;

    @Column(
        name = "user_status",
        nullable = false,
        length = 20
    )
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Status status;

    @Column(
        name = "user_email",
        nullable = false,
        length = 100
    )
    private String email;

    @Column(
        name = "user_address",
        nullable = false,
        length = 255
    )
    private String address;

    @OneToMany(
        mappedBy = "assignedTo",
        cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
        },
        fetch = FetchType.LAZY
    )
    private List<Task> tasksAssigned = new ArrayList<>();

    @Column(
        name = "joining_date",
        nullable = false
    )
    private LocalDate joinDate;

    @Column(name = "resign_date")
    private LocalDate resignDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "manager_id",
        foreignKey = @ForeignKey(name = "fk_user_manager"),
        referencedColumnName = "id"
    )
    private User manager;

    @OneToMany(
        mappedBy = "manager",
        fetch = FetchType.LAZY
    )
    private List<User> subordinates = new ArrayList<>();

    @OneToMany(
        mappedBy = "assignedBy",
        fetch = FetchType.LAZY,
        cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
        }
    )
    private List<Task> assignedTaskByMe = new ArrayList<>();

    public List<Task> getTasksAssigned(){
        if(tasksAssigned == null || tasksAssigned.isEmpty()){
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(tasksAssigned);
    }

    public List<User> getSubordinates(){
        if(subordinates == null || subordinates.isEmpty()){
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(subordinates);
    }

    public List<Task> getAssignedTaskByMe(){
        if(assignedTaskByMe == null || assignedTaskByMe.isEmpty()){
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(assignedTaskByMe);
    }
}
