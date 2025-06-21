package ct.migratordesktop.toderby;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import ct.migratordesktop.swing.AbstractPanel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Slf4j
@org.springframework.stereotype.Component
public class DerbyPanel extends AbstractPanel {
	private static final long	serialVersionUID	= 1L;
	@Lazy
	@Autowired
	private DerbyServiceImpl	derbyService;

	public DerbyPanel() {
		super();
		//
		final var exportBt = new JButton( "Derby  (Ecostat -> Derby)" );
		exportBt.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent event ) {
				final var actionName = ((JButton)event.getSource()).getText();
				final var result = JOptionPane.showInternalConfirmDialog( null, "Futtatható ?", actionName, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
				if ( result == JOptionPane.YES_OPTION )
					new Thread( () -> {
						start( actionName );
						derbyService.toDerby();
						stop();
					} ).start();
			}
		} );
		buttonPanel.add( exportBt );

		final var compareBt = new JButton( "Compare" );
		compareBt.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent event ) {
				final var actionName = ((JButton)event.getSource()).getText();
				new Thread( () -> {
					start( actionName );
					derbyService.compare();
					stop();
				} ).start();
			}
		} );
		buttonPanel.add( compareBt );
	}

}