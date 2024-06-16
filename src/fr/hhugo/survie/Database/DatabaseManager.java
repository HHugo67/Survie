package fr.hhugo.survie.Database;

import fr.hhugo.survie.Survie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager
{
    private Connection connection;

    private final static Survie plugin = Survie.getInstance();
    private final static DatabaseManager INSTANCE = new DatabaseManager();

    private DatabaseManager() {}

    public void connect(String path)
    {
        try
        {
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            createTables();
        }
        catch (SQLException ex)
        {
            plugin.getLogger().severe(Survie.ANSI_WHITE_BACKGROUND + Survie.ANSI_RED
                    + ex.getMessage() + Survie.ANSI_RESET);
        }

        plugin.getLogger().info(Survie.ANSI_WHITE_BACKGROUND + Survie.ANSI_GREEN
                + "Connexion à la BDD..." + Survie.ANSI_RESET);
    }

    public void disconnect()
    {
        try
        {
            connection.close();
        }
        catch (SQLException ex)
        {
            plugin.getLogger().severe(Survie.ANSI_WHITE_BACKGROUND + Survie.ANSI_RED
                    + ex.getMessage() + Survie.ANSI_RESET);
        }

        plugin.getLogger().info(Survie.ANSI_WHITE_BACKGROUND + Survie.ANSI_RED
                + "Déconnexion à la BDD..." + Survie.ANSI_RESET);
    }

    private void createTables()
    {
        try(Statement stmt = connection.createStatement())
        {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS JOUEURS (UUID TEXT PRIMARY KEY NOT NULL, " +
                    "Name TEXT NOT NULL)");
        }
        catch (SQLException ex)
        {
            plugin.getLogger().severe(Survie.ANSI_WHITE_BACKGROUND + Survie.ANSI_RED
                    + ex.getMessage() + Survie.ANSI_RESET);
        }
    }

    public void addPlayer(String uuid, String name)
    {
        try (PreparedStatement statement = connection.
                prepareStatement("INSERT OR IGNORE INTO JOUEURS " +
                        "(UUID, Name) VALUES (?, ?)")) {
            statement.setString(1, uuid);
            statement.setString(2, name);
            statement.executeUpdate();
        }
        catch (SQLException ex)
        {
            plugin.getLogger().severe(Survie.ANSI_WHITE_BACKGROUND + Survie.ANSI_RED
                    + ex.getMessage() + Survie.ANSI_RESET);
        }
    }

    public List<UUID> getAllPlayers()
    {
        List<UUID> players = new ArrayList<>();
        try(Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT UUID FROM JOUEURS"))
        {
            while(resultSet.next())
            {
                UUID uuid = UUID.fromString(resultSet.getString("UUID"));
                players.add(uuid);
            }
        }
        catch (SQLException ex)
        {
            plugin.getLogger().severe(Survie.ANSI_WHITE_BACKGROUND + Survie.ANSI_RED
                    + ex.getMessage() + Survie.ANSI_RESET);
        }

        return players;
    }

    public static DatabaseManager getInstance()
    {
        return INSTANCE;
    }
}