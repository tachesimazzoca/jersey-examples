package app.models;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.codec.digest.DigestUtils;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    private String email = "";

    @Column(name = "password_salt")
    private String passwordSalt = "";

    @Column(name = "password_hash")
    private String passwordHash = "";

    @Convert(converter = User.StatusConverter.class)
    private Status status = Status.ACTIVATED;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void updatePassword(String password) {
        setPasswordHash(DigestUtils.sha1Hex(getPasswordSalt() + password));
    }

    public void updatePassword(String password, String salt) {
        setPasswordSalt(salt);
        setPasswordHash(DigestUtils.sha1Hex(salt + password));
    }

    public enum Status {
        INACTIVATED(0), ACTIVATED(1), VERIFYING(2);

        private int value;

        private Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Status fromValue(int v) {
            for (Status s : Status.values()) {
                if (s.getValue() == v) {
                    return s;
                }
            }
            throw new IllegalArgumentException("unknown value: " + v);
        }
    }

    public static class StatusConverter implements
            AttributeConverter<Status, Integer> {
        @Override
        public Integer convertToDatabaseColumn(Status status) {
            return status.getValue();
        }

        @Override
        public Status convertToEntityAttribute(Integer value) {
            return Status.fromValue(value);
        }
    }
}
