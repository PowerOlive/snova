/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: AddGroup.java 
 *
 * @author yinqiwen [ 2010-4-9 | 10:23:41 PM]
 *
 */
package org.snova.framework.admin.gae.handler;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.snova.framework.admin.gae.GAEAdmin;
import org.snova.framework.admin.gae.auth.Group;
import org.snova.framework.admin.gae.auth.Operation;
import org.snova.framework.event.gae.GroupOperationEvent;
import org.snova.framework.proxy.gae.GAERemoteHandler;


/**
 *
 */
public class DeleteGroup implements CommandHandler
{
	
	public static final String	COMMAND	= "groupdel";
	private Options				options	= new Options();
	
	private GAERemoteHandler connection;
	public DeleteGroup(GAERemoteHandler connection)
	{
		this.connection = connection;
		options.addOption("h", "help", false, "print this message.");
	}
	
	@Override
	public void execute(String[] args)
	{
		CommandLineParser parser = new PosixParser();

		try
		{
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			// validate that block-size has been set
			if(line.hasOption("h"))
			{
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(COMMAND + " groupname", options);
			}
			else
			{
				String[] groupnameargs = line.getArgs();
				if(groupnameargs != null && groupnameargs.length != 1)
				{
					GAEAdmin.outputln("Argument groupname required!");
				}
				GroupOperationEvent event = new GroupOperationEvent();
				event.opr = Operation.DELETE;
				event.grp = new Group();
				event.grp.setName(groupnameargs[0]);
				AdminResponseEventHandler.syncSendEvent(connection, event);
			}
		}
		catch(Exception exp)
		{
			System.out.println("Error:" + exp.getMessage());
		}
	}

	@Override
	public void printHelp()
	{
		// TODO Auto-generated method stub
		
	}

}
