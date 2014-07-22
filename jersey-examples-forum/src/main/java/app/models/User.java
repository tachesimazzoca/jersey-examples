package app.models;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;

@Entity
@Table(name = "users")
public class User {
    private static final int PASSWORD_SALT_LENGTH = 4;

    @Id
    @GeneratedValue
    private Long id;

    private String email = "";

    @Column(name = "password_salt")
    private String passwordSalt = "";

    @Column(name = "password_hash")
    private String passwordHash = "";

    @Convert(converter = User.StatusConverter.class)
    private Status status = Status.ACTIVE;

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

    public void refreshPassword(String password) {
        setPasswordSalt(RandomStringUtils.randomAlphanumeric(PASSWORD_SALT_LENGTH));
        setPasswordHash(hashPassword(password, getPasswordSalt()));
    }

    public void refreshPassword(String password, String salt) {
        if (salt == null || salt.length() != PASSWORD_SALT_LENGTH)
            throw new IllegalArgumentException(
                    "The length of the parameter salt must be equal to " + PASSWORD_SALT_LENGTH);
        setPasswordSalt(salt);
        setPasswordHash(hashPassword(password, getPasswordSalt()));
    }

    public boolean isEqualPassword(String password) {
        return hashPassword(password, getPasswordSalt()).equals(getPasswordHash());
    }

    private String hashPassword(String password, String salt) {
        return DigestUtils.sha1Hex(salt + password);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((passwordHash == null) ? 0 : passwordHash.hashCode());
        result = prime * result + ((passwordSalt == null) ? 0 : passwordSalt.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (passwordHash == null) {
            if (other.passwordHash != null)
                return false;
        } else if (!passwordHash.equals(other.passwordHash))
            return false;
        if (passwordSalt == null) {
            if (other.passwordSalt != null)
                return false;
        } else if (!passwordSalt.equals(other.passwordSalt))
            return false;
        if (status != other.status)
            return false;
        return true;
    }

    public enum Status {
        INACTIVE(0), ACTIVE(1);

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
