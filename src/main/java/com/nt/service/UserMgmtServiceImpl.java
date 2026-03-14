package com.nt.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.nt.bindings.ActiveUser;
import com.nt.bindings.LoginCredentials;
import com.nt.bindings.RecoverPassword;
import com.nt.bindings.UserAccount;
import com.nt.entity.UserMaster;
import com.nt.repository.IUserMasterRepository;
import com.nt.utils.EmailUtils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class UserMgmtServiceImpl implements IUserMgmtService {

	@Autowired
   private Environment env;
	@Autowired
	private IUserMasterRepository userMasterRepo;

	@Autowired
	private EmailUtils emailUtils;

	@Override
	public String registerUser(UserAccount user) throws Exception {
		//Convert UserAccount obj datra to UserMaster obj data
		UserMaster master=new UserMaster();
		BeanUtils.copyProperties(user, master);
		//set random String of 6 chars as password
		master.setPassword(generateRandomPassword(6));
		master.setActive_sw("Active");
		master.setCreateBy(System.getProperty("user.name"));
		
		//save Obj
		UserMaster savedMaster=userMasterRepo.save(master);
		//perform the mailOperation
		
		String subject="User Registration Success";
		String body=readEmailMessageBody(env.getProperty("spring.mailbody.registeruser.location"),user.getName(),master.getPassword());
		String toEmail = user.getEmail();
		if (toEmail == null || toEmail.isBlank()) {
		    throw new IllegalArgumentException("User email is null or empty. Cannot send email.");
		}
		emailUtils.sendEmailMessage(toEmail,subject,body);
	
		//return message
		return savedMaster!=null?"User is registed with id value::"+savedMaster.getUserId():"Problem in User registration";
	}

	@Override
	public String activeUserAccount(ActiveUser user) {
		//user findBy method
		UserMaster entity=userMasterRepo.findByEmailAndPassword(user.getEmail(), user.getTempPassword());
	
		if(entity==null)
		{
			return "User is not found for activation";
		}
		else
		{
			//set the password
			entity.setPassword(user.getConfirmPassword());
			//chnage the user account status
			entity.setActive_sw("Active");
			
			//update the obj
			UserMaster updatedEntity=userMasterRepo.save(entity);
			return "User is activated with new password";
		}
	}

	@Override
	public String login(LoginCredentials credentials) {
		//convert LoginCredentials into UserMaster obj (entity obj)
		UserMaster master=new UserMaster();
		BeanUtils.copyProperties(credentials, master);
		//prepare Example obj
		Example<UserMaster> example=Example.of(master);
		List<UserMaster> listEntities=userMasterRepo.findAll(example);
		if(listEntities.size()==0)
		{
			return "Invalid Credentials";
		}
		else
		{
			//get entity obj
			UserMaster entity=listEntities.get(0);
			if(entity.getActive_sw().equalsIgnoreCase("Active"))
			{
				return "Valid credentials and Login successful";
			}
			else
			{
				return "User Account is not Active";
			}
		}
	}

	@Override
	public List<UserAccount> listUsers() {
		//Load all entities and convert to UserAccount obj
		List<UserAccount> listUsers=userMasterRepo.findAll().stream().map(entity->{
			UserAccount user=new UserAccount();
			BeanUtils.copyProperties(entity,user);
			return user;
		}).toList();
	return listUsers;
	}

	@Override
	public UserAccount showUserByUserId(Integer id) {
		//Load the userBy userId
		Optional<UserMaster> opt=userMasterRepo.findById(id);
		UserAccount account=null;
		if(opt.isPresent())
		{
			account=new UserAccount();
			BeanUtils.copyProperties(opt.get(), account);
		}
		return account;
	}

	@Override
	public UserAccount showUserByEmailAndName(String email, String name) {
		//use custom findBy method
		
		UserMaster master=userMasterRepo.findByNameAndEmail(name, email);
		UserAccount account=null;
		if(master!=null)
		{
			account=new UserAccount();
			BeanUtils.copyProperties(master, account);
		}
		return account;
	}

	@Override
	public String updateUser(UserAccount user) {
		//use custom fing by method
		Optional<UserMaster> opt=userMasterRepo.findByName(user.getName());
		if(opt.isPresent())
		{
			//get the entity
			UserMaster master=opt.get();
			BeanUtils.copyProperties(user, master);
			userMasterRepo.save(master);
			return "User Details are updated";
		}
		else
		{
			return "User not found for updation";
		}
	}

	@Override
	public String deleteUserById(Integer id) {
	  //load the obj
		Optional<UserMaster> opt=userMasterRepo.findById(id);
		if(opt.isPresent())
		{
			userMasterRepo.deleteById(id);
			return "User is deleted";
			
		}
		return "User is not found for deletion";
	}

	@Override
	public String changeUserStatus(Integer id, String status) {
	//load the obj
		Optional<UserMaster> opt=userMasterRepo.findById(id);
		if(opt.isPresent())
		{
			//get entity obj
			UserMaster master=opt.get();
			//change the status
			master.setActive_sw(status);
			//update the obj
			userMasterRepo.save(master);
			return "User Status changed";
		}
		return "User not found for changing the status";
	}

	@Override
	public String recoverPassword(RecoverPassword recover) throws Exception {
		//get entity obj by email and name
		UserMaster master=userMasterRepo.findByNameAndEmail(recover.getName(), recover.getEmail());
		
	if(master !=null)
	{
		String pwd=master.getPassword();
		//send the recovered password to the email account
		String subject="Mail for Password Recovery";
		String mailBody=readEmailMessageBody(env.getProperty("spring.mailbody.recoverpwd.location"),recover.getName(),pwd);
		
		String toEmail = master.getEmail();
		if (toEmail == null || toEmail.isBlank()) {
		    throw new IllegalArgumentException("User email is null or empty. Cannot send email.");
		}
		emailUtils.sendEmailMessage(toEmail,subject,mailBody);
		return pwd;
	}
		return "User and email is not found";
	}
	
	//helper method for same class
	private String generateRandomPassword(int length)
	{
		//a list of character to choose from in form of a String
		String AlphaNumericstr="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstivwxyz0123456789";
		//creating string Buffer size of alphanumericStr
		StringBuilder randomWord=new StringBuilder(length);
		
		for(int i=0;i<length;i++)
		{
			int ch=(int) (AlphaNumericstr.length()*Math.random());
			
			//adding random caharacter one by one at the end of randomWord
			randomWord.append(AlphaNumericstr.charAt(ch));
	}
		return randomWord.toString();
	}
	
	
	private String readEmailMessageBody(String fileName,String fullName,String pwd) throws Exception{
		  if (fileName == null) {
		        throw new Exception("Email template file name is null");
		    }

		    // Remove "classpath:" prefix if present
		    if(fileName.startsWith("classpath:")) {
		        fileName = fileName.substring("classpath:".length());
		    }

		    // Load file from classpath
		    Resource resource = new ClassPathResource(fileName);

		    StringBuilder buffer = new StringBuilder();
		    try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
		        String line;
		        while ((line = br.readLine()) != null) {
		            buffer.append(line).append("\n");
		        }
		    }

		    String mailBody = buffer.toString();
		
		    // safe null replacement
		    mailBody = mailBody.replace("{FULL-NAME}", fullName != null ? fullName : "")
		                       .replace("{PWD}", pwd != null ? pwd : "")
		                       .replace("{URL}", "");  // you can set a real URL if needed

		    return mailBody;
		}
	
}
