package projectJDBC;

import javax.swing.*;

import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class studentRecords extends JFrame {
    private static final long serialVersionUID = 1L;

    public studentRecords() {
        setTitle("Course Enrollment System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Students", new StudentPanel());
        tabbedPane.add("Courses", new CoursePanel());
        tabbedPane.add("Enrollments", new EnrollmentPanel());

        add(tabbedPane);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(studentRecords::new);
    }

    static class StudentPanel extends JPanel {
        private static final long serialVersionUID = 1L;

            private JTextField txtStudentId, txtFirstName, txtLastName, txtMiddleName, txtSearch;
            private JTable table;
            private DefaultTableModel model;
            private final Color primaryColor = new Color(59, 130, 246);
            private final Font font = new Font("Segoe UI", Font.PLAIN, 14);

            public StudentPanel() {
                setLayout(new BorderLayout(15, 15));
                setBorder(new EmptyBorder(20, 20, 20, 20));
                setBackground(Color.WHITE);

                JLabel header = new JLabel("Student Management");
                header.setFont(new Font("Segoe UI", Font.BOLD, 24));
                header.setForeground(primaryColor);
                add(header, BorderLayout.NORTH);

                JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
                searchPanel.setBackground(Color.WHITE);
                txtSearch = new JTextField();
                txtSearch.setFont(font);
                txtSearch.setToolTipText("Search students by ID, First Name, Last Name, or Middle Name");
                JLabel lblSearch = new JLabel("Search:");
                lblSearch.setFont(font.deriveFont(Font.BOLD));
                searchPanel.add(lblSearch, BorderLayout.WEST);
                searchPanel.add(txtSearch, BorderLayout.CENTER);
                add(searchPanel, BorderLayout.SOUTH);

                JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
                centerPanel.setBackground(Color.WHITE);

                JPanel formPanel = new JPanel(new GridBagLayout());
                formPanel.setBackground(Color.WHITE);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(8, 8, 8, 8);
                gbc.fill = GridBagConstraints.HORIZONTAL;

                txtStudentId = new JTextField(15);
                txtFirstName = new JTextField(15);
                txtLastName = new JTextField(15);
                txtMiddleName = new JTextField(15);


                int row = 0;

                gbc.gridx = 0; gbc.gridy = row;
                formPanel.add(createLabel("Student ID:"), gbc);
                gbc.gridx = 1;
                formPanel.add(txtStudentId, gbc);

                row++;
                gbc.gridx = 0; gbc.gridy = row;
                formPanel.add(createLabel("First Name:"), gbc);
                gbc.gridx = 1;
                formPanel.add(txtFirstName, gbc);

                row++;
                gbc.gridx = 0; gbc.gridy = row;
                formPanel.add(createLabel("Last Name:"), gbc);
                gbc.gridx = 1;
                formPanel.add(txtLastName, gbc);

                row++;
                gbc.gridx = 0; gbc.gridy = row;
                formPanel.add(createLabel("Middle Name:"), gbc);
                gbc.gridx = 1;
                formPanel.add(txtMiddleName, gbc);


                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
                btnPanel.setBackground(Color.WHITE);
                JButton btnAdd = createButton("Add");
                JButton btnUpdate = createButton("Update");
                JButton btnDelete = createButton("Delete");
                btnPanel.add(btnAdd);
                btnPanel.add(btnUpdate);
                btnPanel.add(btnDelete);

                JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
                leftPanel.setBackground(Color.WHITE);
                leftPanel.add(formPanel, BorderLayout.CENTER);
                leftPanel.add(btnPanel, BorderLayout.SOUTH);
                leftPanel.setPreferredSize(new Dimension(360, 0));

                centerPanel.add(leftPanel, BorderLayout.WEST);


                model = new DefaultTableModel(new String[]{"No.", "Student ID", "First Name", "Last Name", "Middle Name"}, 0) {
                    /**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
                    public boolean isCellEditable(int row, int column) {
                        return false; 
                    }
                };
                table = new JTable(model);
                styleTable(table);

                JScrollPane tableScroll = new JScrollPane(table);
                centerPanel.add(tableScroll, BorderLayout.CENTER);

                add(centerPanel, BorderLayout.CENTER);

                loadStudents();

                btnAdd.addActionListener(e -> addStudent());
                btnUpdate.addActionListener(e -> updateStudent());
                btnDelete.addActionListener(e -> deleteStudent());

                table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());

                txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                    public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
                    public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
                    public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
                });
            }

            private void filterTable() {
                String query = txtSearch.getText().toLowerCase().trim();
                model.setRowCount(0);
                String sql = "SELECT * FROM students WHERE " +
                        "LOWER(student_id) LIKE ? OR LOWER(firstname) LIKE ? OR LOWER(lastname) LIKE ? OR LOWER(middle_name) LIKE ?";
                try (Connection conn = connectDB();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    String likeQuery = "%" + query + "%";
                    for (int i = 1; i <= 4; i++) {
                        ps.setString(i, likeQuery);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            model.addRow(new Object[]{
                                    rs.getInt("id"),
                                    rs.getString("student_id"),
                                    rs.getString("firstname"),
                                    rs.getString("lastname"),
                                    rs.getString("middle_name")
                            });
                        }
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error searching students: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            private JLabel createLabel(String text) {
                JLabel lbl = new JLabel(text);
                lbl.setFont(font.deriveFont(Font.BOLD));
                return lbl;
            }

            private JButton createButton(String text) {
                JButton btn = new JButton(text);
                btn.setFont(font.deriveFont(Font.BOLD));
                btn.setBackground(primaryColor);
                btn.setForeground(Color.WHITE);
                btn.setFocusPainted(false);
                btn.setPreferredSize(new Dimension(100, 35));
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        btn.setBackground(primaryColor.darker());
                    }

                    public void mouseExited(MouseEvent e) {
                        btn.setBackground(primaryColor);
                    }
                });
                return btn;
            }

            private void styleTable(JTable table) {
                table.setFont(font);
                table.setRowHeight(28);
                table.getTableHeader().setFont(font.deriveFont(Font.BOLD));
                table.getTableHeader().setBackground(primaryColor);
                table.getTableHeader().setForeground(Color.WHITE);
                table.setSelectionBackground(primaryColor.brighter());
                table.setSelectionForeground(Color.WHITE);
            }

            private void fillFormFromTable() {
                int row = table.getSelectedRow();
                if (row < 0) return;

                txtStudentId.setText(model.getValueAt(row, 1).toString());
                txtFirstName.setText(model.getValueAt(row, 2).toString());
                txtLastName.setText(model.getValueAt(row, 3).toString());
                txtMiddleName.setText(model.getValueAt(row, 4).toString());
            }

            private void loadStudents() {
                model.setRowCount(0);
                String sql = "SELECT * FROM students ORDER BY student_id";
                try (Connection conn = connectDB();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getInt("id"),
                                rs.getString("student_id"),
                                rs.getString("firstname"),
                                rs.getString("lastname"),
                                rs.getString("middle_name")
                        });
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            private void addStudent() {
                String sid = txtStudentId.getText().trim();
                String fname = txtFirstName.getText().trim();
                String lname = txtLastName.getText().trim();
                String mname = txtMiddleName.getText().trim();

                if (sid.isEmpty() || fname.isEmpty() || lname.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Student ID, First Name, and Last Name are required.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String sql = "INSERT INTO students (student_id, firstname, lastname, middle_name) VALUES (?, ?, ?, ?)";
                try (Connection conn = connectDB();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, sid);
                    ps.setString(2, fname);
                    ps.setString(3, lname);
                    ps.setString(4, mname.isEmpty() ? null : mname);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Student added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadStudents();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error adding student: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            private void updateStudent() {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Select a student to update.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int dbId = (int) model.getValueAt(row, 0);

                String sid = txtStudentId.getText().trim();
                String fname = txtFirstName.getText().trim();
                String lname = txtLastName.getText().trim();
                String mname = txtMiddleName.getText().trim();

                if (sid.isEmpty() || fname.isEmpty() || lname.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Student ID, First Name, and Last Name are required.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String sql = "UPDATE students SET student_id=?, firstname=?, lastname=?, middle_name=? WHERE id=?";
                try (Connection conn = connectDB();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, sid);
                    ps.setString(2, fname);
                    ps.setString(3, lname);
                    ps.setString(4, mname.isEmpty() ? null : mname);
                    ps.setInt(5, dbId);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Student updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadStudents();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error updating student: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            private void deleteStudent() {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Select a student to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int dbId = (int) model.getValueAt(row, 0);

                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure to delete the selected student?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;

                String sql = "DELETE FROM students WHERE id=?";
                try (Connection conn = connectDB();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, dbId);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Student deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadStudents();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error deleting student: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            private void clearForm() {
                txtStudentId.setText("");
                txtFirstName.setText("");
                txtLastName.setText("");
                txtMiddleName.setText("");
                table.clearSelection();
            }

            private Connection connectDB() throws SQLException {
            	  String url = "jdbc:mysql://localhost:3306/student_db";
                  String user = "root";
                  String pass = "";
                return DriverManager.getConnection(url, user, pass);
            }
        }

        static class CoursePanel extends JPanel {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			private JTextField txtCourseCode, txtCourseName, txtInstructor, txtSearch;
            private JTable table;
            private DefaultTableModel model;
            private final Color primaryColor = new Color(59, 130, 246);
            private final Font font = new Font("Segoe UI", Font.PLAIN, 14);

            public CoursePanel() {
                setLayout(new BorderLayout(15, 15));
                setBorder(new EmptyBorder(20, 20, 20, 20));
                setBackground(Color.WHITE);

                JLabel header = new JLabel("Course Management");
                header.setFont(new Font("Segoe UI", Font.BOLD, 24));
                header.setForeground(primaryColor);
                add(header, BorderLayout.NORTH);

                // Search bar
                JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
                searchPanel.setBackground(Color.WHITE);
                txtSearch = new JTextField();
                txtSearch.setFont(font);
                txtSearch.setToolTipText("Search courses by code or name");
                JLabel lblSearch = new JLabel("Search:");
                lblSearch.setFont(font.deriveFont(Font.BOLD));
                searchPanel.add(lblSearch, BorderLayout.WEST);
                searchPanel.add(txtSearch, BorderLayout.CENTER);
                add(searchPanel, BorderLayout.SOUTH);

                JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
                centerPanel.setBackground(Color.WHITE);

                JPanel formPanel = new JPanel(new GridBagLayout());
                formPanel.setBackground(Color.WHITE);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(8, 8, 8, 8);
                gbc.fill = GridBagConstraints.HORIZONTAL;

                txtCourseCode = new JTextField(15);
                txtCourseName = new JTextField(15);
                txtInstructor = new JTextField(15);

                int row = 0;
                gbc.gridx = 0; gbc.gridy = row;
                formPanel.add(createLabel("Course Code:"), gbc);
                gbc.gridx = 1;
                formPanel.add(txtCourseCode, gbc);

                row++;
                gbc.gridx = 0; gbc.gridy = row;
                formPanel.add(createLabel("Course Name:"), gbc);
                gbc.gridx = 1;
                formPanel.add(txtCourseName, gbc);

                row++;
                gbc.gridx = 0; gbc.gridy = row;
                formPanel.add(createLabel("Instructor:"), gbc);
                gbc.gridx = 1;
                formPanel.add(txtInstructor, gbc);

                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
                btnPanel.setBackground(Color.WHITE);
                JButton btnAdd = createButton("Add");
                JButton btnUpdate = createButton("Update");
                JButton btnDelete = createButton("Delete");
                btnPanel.add(btnAdd);
                btnPanel.add(btnUpdate);
                btnPanel.add(btnDelete);

                JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
                leftPanel.setBackground(Color.WHITE);
                leftPanel.add(formPanel, BorderLayout.CENTER);
                leftPanel.add(btnPanel, BorderLayout.SOUTH);
                leftPanel.setPreferredSize(new Dimension(360, 0));

                centerPanel.add(leftPanel, BorderLayout.WEST);

                model = new DefaultTableModel(new String[]{"ID", "Course Code", "Course Name", "Instructor"}, 0) {
                    /**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                table = new JTable(model);
                styleTable(table);

                JScrollPane scrollPane = new JScrollPane(table);
                centerPanel.add(scrollPane, BorderLayout.CENTER);

                add(centerPanel, BorderLayout.CENTER);

                loadCourses();

                btnAdd.addActionListener(e -> addCourse());
                btnUpdate.addActionListener(e -> updateCourse());
                btnDelete.addActionListener(e -> deleteCourse());

                table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());

                txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                    public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
                    public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
                    public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
                });
            }

            private void filterTable() {
                String query = txtSearch.getText().toLowerCase().trim();
                model.setRowCount(0);
                String sql = "SELECT * FROM courses WHERE LOWER(course_code) LIKE ? OR LOWER(course_name) LIKE ?";
                try (Connection conn = connectDB();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    String likeQuery = "%" + query + "%";
                    ps.setString(1, likeQuery);
                    ps.setString(2, likeQuery);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            model.addRow(new Object[]{
                                    rs.getInt("id"),
                                    rs.getString("course_code"),
                                    rs.getString("course_name"),
                                    rs.getString("instructor")
                            });
                        }
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error searching courses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            private JLabel createLabel(String text) {
                JLabel lbl = new JLabel(text);
                lbl.setFont(font.deriveFont(Font.BOLD));
                return lbl;
            }

            private JButton createButton(String text) {
                JButton btn = new JButton(text);
                btn.setFont(font.deriveFont(Font.BOLD));
                btn.setBackground(primaryColor);
                btn.setForeground(Color.WHITE);
                btn.setFocusPainted(false);
                btn.setPreferredSize(new Dimension(100, 35));
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        btn.setBackground(primaryColor.darker());
                    }

                    public void mouseExited(MouseEvent e) {
                        btn.setBackground(primaryColor);
                    }
                });
                return btn;
            }

            private void styleTable(JTable table) {
                table.setFont(font);
                table.setRowHeight(28);
                table.getTableHeader().setFont(font.deriveFont(Font.BOLD));
                table.getTableHeader().setBackground(primaryColor);
                table.getTableHeader().setForeground(Color.WHITE);
                table.setSelectionBackground(primaryColor.brighter());
                table.setSelectionForeground(Color.WHITE);
            }

            private void fillFormFromTable() {
                int row = table.getSelectedRow();
                if (row < 0) return;

                txtCourseCode.setText(model.getValueAt(row, 1).toString());
                txtCourseName.setText(model.getValueAt(row, 2).toString());
                txtInstructor.setText(model.getValueAt(row, 3).toString());
            }

            private void loadCourses() {
                model.setRowCount(0);
                String sql = "SELECT * FROM courses ORDER BY course_code";
                try (Connection conn = connectDB();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getInt("id"),
                                rs.getString("course_code"),
                                rs.getString("course_name"),
                                rs.getString("instructor")
                        });
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            private void addCourse() {
                String code = txtCourseCode.getText().trim();
                String name = txtCourseName.getText().trim();
                String instructor = txtInstructor.getText().trim();

                if (code.isEmpty() || name.isEmpty() || instructor.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Course Code, Name, and Instructor are required.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String sql = "INSERT INTO courses (course_code, course_name, instructor) VALUES (?, ?, ?)";
                try (Connection conn = connectDB();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, code);
                    ps.setString(2, name);
                    ps.setString(3, instructor);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Course added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadCourses();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error adding course: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            private void updateCourse() {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Select a course to update.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int dbId = (int) model.getValueAt(row, 0);

                String code = txtCourseCode.getText().trim();
                String name = txtCourseName.getText().trim();
                String instructor = txtInstructor.getText().trim();

                if (code.isEmpty() || name.isEmpty() || instructor.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Course Code, Name, and Instructor are required.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String sql = "UPDATE courses SET course_code=?, course_name=?, instructor=? WHERE id=?";
                try (Connection conn = connectDB();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, code);
                    ps.setString(2, name);
                    ps.setString(3, instructor);
                    ps.setInt(4, dbId);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Course updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadCourses();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error updating course: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            private void deleteCourse() {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Select a course to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int dbId = (int) model.getValueAt(row, 0);

                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure to delete the selected course?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;

                String sql = "DELETE FROM courses WHERE id=?";
                try (Connection conn = connectDB();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, dbId);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Course deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadCourses();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error deleting course: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            private void clearForm() {
                txtCourseCode.setText("");
                txtCourseName.setText("");
                txtInstructor.setText("");
                table.clearSelection();
            }

            // Database connection method
            private Connection connectDB() throws SQLException {
                String url = "jdbc:mysql://localhost:3306/student_db";
                String user = "root";
                String pass = "";
                return DriverManager.getConnection(url, user, pass);
            }
        }


	        static class EnrollmentPanel extends JPanel {
	            /**
				 * 
				 */
				private static final long serialVersionUID = 1L;
				private JComboBox<String> cbStudents, cbCourses;
	            private JButton btnEnroll, btnDeleteEnrollment;
	            private JTable table;
	            private DefaultTableModel model;
	            private final Color primaryColor = new Color(59, 130, 246);
	            private final Font font = new Font("Segoe UI", Font.PLAIN, 14);
	
	            public EnrollmentPanel() {
	                setLayout(new BorderLayout(15, 15));
	                setBorder(new EmptyBorder(20, 20, 20, 20));
	                setBackground(Color.WHITE);
	
	                JLabel header = new JLabel("Course Enrollment");
	                header.setFont(new Font("Segoe UI", Font.BOLD, 24));
	                header.setForeground(primaryColor);
	                add(header, BorderLayout.NORTH);
	
	                JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
	                topPanel.setBackground(Color.WHITE);
	
	                cbStudents = new JComboBox<>();
	                cbCourses = new JComboBox<>();
	                btnEnroll = new JButton("Enroll");
	                btnDeleteEnrollment = new JButton("Delete Enrollment");
	
	                styleButton(btnEnroll);
	                styleButton(btnDeleteEnrollment);
	
	                topPanel.add(new JLabel("Student:"));
	                topPanel.add(cbStudents);
	                topPanel.add(new JLabel("Course:"));
	                topPanel.add(cbCourses);
	                topPanel.add(btnEnroll);
	                topPanel.add(btnDeleteEnrollment);
	
	                add(topPanel, BorderLayout.NORTH);
	
	                model = new DefaultTableModel(new String[]{"Enrollment ID", "Student ID", "Student Name", "Course Code", "Course Name"}, 0) {
	                    /**
						 * 
						 */
						private static final long serialVersionUID = 1L;
	
						@Override
	                    public boolean isCellEditable(int row, int column) {
	                        return false;
	                    }
	                };
	                table = new JTable(model);
	                styleTable(table);
	
	                JScrollPane scrollPane = new JScrollPane(table);
	                add(scrollPane, BorderLayout.CENTER);
	
	                loadStudents();
	                loadCourses();
	                loadEnrollments();
	
	                btnEnroll.addActionListener(e -> enrollStudent());
	                btnDeleteEnrollment.addActionListener(e -> deleteEnrollment());
	            }
	
	            private void styleButton(JButton btn) {
	                btn.setFont(font.deriveFont(Font.BOLD));
	                btn.setBackground(primaryColor);
	                btn.setForeground(Color.WHITE);
	                btn.setFocusPainted(false);
	                btn.setPreferredSize(new Dimension(140, 35));
	                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	                btn.addMouseListener(new MouseAdapter() {
	                    public void mouseEntered(MouseEvent e) {
	                        btn.setBackground(primaryColor.darker());
	                    }
	
	                    public void mouseExited(MouseEvent e) {
	                        btn.setBackground(primaryColor);
	                    }
	                });
	            }
	
	            private void styleTable(JTable table) {
	                table.setFont(font);
	                table.setRowHeight(28);
	                table.getTableHeader().setFont(font.deriveFont(Font.BOLD));
	                table.getTableHeader().setBackground(primaryColor);
	                table.getTableHeader().setForeground(Color.WHITE);
	                table.setSelectionBackground(primaryColor.brighter());
	                table.setSelectionForeground(Color.WHITE);
	            }
	
	            private void loadStudents() {
	                cbStudents.removeAllItems();
	                String sql = "SELECT id, student_id, firstname, lastname FROM students ORDER BY student_id";
	                try (Connection conn = connectDB();
	                     Statement stmt = conn.createStatement();
	                     ResultSet rs = stmt.executeQuery(sql)) {
	                    while (rs.next()) {
	                        String display = rs.getString("student_id") + " - " + rs.getString("firstname") + " " + rs.getString("lastname");
	                        cbStudents.addItem(display + ":" + rs.getInt("id"));  // Store ID after colon
	                    }
	                } catch (SQLException e) {
	                    JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	                }
	            }
	
	            private void loadCourses() {
	                cbCourses.removeAllItems();
	                String sql = "SELECT id, course_code, course_name FROM courses ORDER BY course_code";
	                try (Connection conn = connectDB();
	                     Statement stmt = conn.createStatement();
	                     ResultSet rs = stmt.executeQuery(sql)) {
	                    while (rs.next()) {
	                        String display = rs.getString("course_code") + " - " + rs.getString("course_name");
	                        cbCourses.addItem(display + ":" + rs.getInt("id"));  // Store ID after colon
	                    }
	                } catch (SQLException e) {
	                    JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	                }
	            }
	
	            private void loadEnrollments() {
	                model.setRowCount(0);
	                String sql = "SELECT e.id AS enrollment_id, s.student_id, CONCAT(s.firstname, ' ', s.lastname) AS student_name, c.course_code, c.course_name " +
	                             "FROM enrollments e " +
	                             "JOIN students s ON e.student_id = s.id " +
	                             "JOIN courses c ON e.course_id = c.id " +
	                             "ORDER BY s.student_id, c.course_code";
	                try (Connection conn = connectDB();
	                     Statement stmt = conn.createStatement();
	                     ResultSet rs = stmt.executeQuery(sql)) {
	                    while (rs.next()) {
	                        model.addRow(new Object[]{
	                                rs.getInt("enrollment_id"),
	                                rs.getString("student_id"),
	                                rs.getString("student_name"),
	                                rs.getString("course_code"),
	                                rs.getString("course_name")
	                        });
	                    }
	                } catch (SQLException e) {
	                    JOptionPane.showMessageDialog(this, "Error loading enrollments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	                }
	            }
	
	            private void enrollStudent() {
	                if (cbStudents.getSelectedItem() == null || cbCourses.getSelectedItem() == null) {
	                    JOptionPane.showMessageDialog(this, "Select both a student and a course.", "Warning", JOptionPane.WARNING_MESSAGE);
	                    return;
	                }
	
	                int studentId = getIdFromComboItem(cbStudents.getSelectedItem().toString());
	                int courseId = getIdFromComboItem(cbCourses.getSelectedItem().toString());
	
	                String checkSql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ?";
	                String insertSql = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";
	                try (Connection conn = connectDB();
	                     PreparedStatement checkPs = conn.prepareStatement(checkSql);
	                     PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
	                    checkPs.setInt(1, studentId);
	                    checkPs.setInt(2, courseId);
	                    try (ResultSet rs = checkPs.executeQuery()) {
	                        if (rs.next() && rs.getInt(1) > 0) {
	                            JOptionPane.showMessageDialog(this, "Student is already enrolled in this course.", "Info", JOptionPane.INFORMATION_MESSAGE);
	                            return;
	                        }
	                    }
	
	                    insertPs.setInt(1, studentId);
	                    insertPs.setInt(2, courseId);
	                    insertPs.executeUpdate();
	                    JOptionPane.showMessageDialog(this, "Enrollment successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
	                    loadEnrollments();
	                } catch (SQLException e) {
	                    JOptionPane.showMessageDialog(this, "Error enrolling student: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	                }
	            }
	
	            private void deleteEnrollment() {
	                int row = table.getSelectedRow();
	                if (row < 0) {
	                    JOptionPane.showMessageDialog(this, "Select an enrollment to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
	                    return;
	                }
	                int enrollmentId = (int) model.getValueAt(row, 0);
	
	                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure to delete the selected enrollment?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
	                if (confirm != JOptionPane.YES_OPTION) return;
	
	                String sql = "DELETE FROM enrollments WHERE id=?";
	                try (Connection conn = connectDB();
	                     PreparedStatement ps = conn.prepareStatement(sql)) {
	                    ps.setInt(1, enrollmentId);
	                    ps.executeUpdate();
	                    JOptionPane.showMessageDialog(this, "Enrollment deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
	                    loadEnrollments();
	                } catch (SQLException e) {
	                    JOptionPane.showMessageDialog(this, "Error deleting enrollment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	                }
	            }
	
	            private int getIdFromComboItem(String item) {
	                int colonIndex = item.lastIndexOf(':');
	                if (colonIndex >= 0) {
	                    try {
	                        return Integer.parseInt(item.substring(colonIndex + 1));
	                    } catch (NumberFormatException ignored) {}
	                }
	                return -1;
	            }
	
	            private Connection connectDB() throws SQLException {
	                String url = "jdbc:mysql://localhost:3306/student_db";
	                String user = "root";
	                String pass = "";
	                return DriverManager.getConnection(url, user, pass);
	            }
	        }
}
