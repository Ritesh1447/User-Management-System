package com.nt.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


import com.nt.entity.UserMaster;

public interface IUserMasterRepository extends JpaRepository<UserMaster, Integer> {

	public UserMaster findByEmailAndPassword(String email,String pwd);
	public UserMaster findByNameAndEmail(String name,String email);
	public Optional<UserMaster> findByName(String name);
	}

