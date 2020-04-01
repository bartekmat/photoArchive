package com.photoarchive.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long photo_id;
    @URL
    private String url;

    @ManyToMany
    @JoinTable(name = "photos_tags", joinColumns = @JoinColumn(name = "photo_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @NotEmpty(message = "please set minimum 1 tag")
    private Set<Tag> tags = new HashSet<>();


}
