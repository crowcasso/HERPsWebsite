<!doctype HTML>
<html>
	<head>
		<title>HERPs - View Data</title>
	</head>
	<body>
		<h1>View Data</h1>
		<form method="GET" action="download">
		<p>Select category:</p>
		<select name="category">
			<option>Aquatic Turtle</option>
			<option>Box Turtle</option>
			<option>Field Data</option>
			<option>Frog Call</option>
			<option>Ephemeral Pool</option>
			<option>Snake</option>
		</select>
		<p>How would you like to view the data?<p>
		<select name="view">
			<option value="web">Web Page</option>
			<option value="xls">Excel Spreadsheet</option>
		</select>
		<p>How should the pictures be included?</p>
		<select name="pictures">
			<option value="link">As Links</option>
			<option value="embed">Embed</option>
		</select>
		<br />
		<input type="submit" value="Submit" />
		<br />
		</form>
	</body>
</html>
