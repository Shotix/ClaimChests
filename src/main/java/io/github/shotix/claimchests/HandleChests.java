package io.github.shotix.claimchests;

import jdk.javadoc.internal.doclets.formats.html.markup.HtmlTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class HandleChests {

    private static Document getDocument() throws IOException, SAXException, ParserConfigurationException {
        File xmlFile = new File("ClaimedChests.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);

        return doc;
    }

    private static void noFile() {
        try {
            // Create a new empty document
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            // Create a Chest element where all chests will be listed
            Element chestElement = doc.createElement("chests");
            doc.appendChild(chestElement);

            // Write the document to a file
            doc.normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("ClaimedChests.xml"));
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isChestPlacementValid(BlockPlaceEvent placedBlock) {
        // Find out if there is a chest next to the current placed chest
        Block block = placedBlock.getBlock();
        Location chestLocation = block.getLocation();
        BlockFace[] relativeFaces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

        for (BlockFace face : relativeFaces) {
            Block block1 = chestLocation.getBlock().getRelative(face);
            if (block1.getType() == Material.CHEST) {
                // Is this chest already a double chest?
                Block blockAt = null;
                switch (face) {
                    case NORTH:
                        blockAt = block.getWorld().getBlockAt(block.getX(), block.getY(), block.getZ() - 1); // get the block to the north of the placed block
                        break;
                    case SOUTH:
                        blockAt = block.getWorld().getBlockAt(block.getX(), block.getY(), block.getZ() + 1); // get the block to the north of the placed block
                        break;
                    case WEST:
                        blockAt = block.getWorld().getBlockAt(block.getX() - 1, block.getY(), block.getZ()); // get the block to the north of the placed block
                        break;
                    case EAST:
                        blockAt = block.getWorld().getBlockAt(block.getX() + 1, block.getY(), block.getZ()); // get the block to the north of the placed block
                        break;
                }
                assert blockAt != null;
                Chest chestAt = (Chest) blockAt.getState(); // cast the block state to a chest
                Inventory inventory = chestAt.getInventory();
                if (!(inventory instanceof DoubleChestInventory)) {
                    // Chest next to the placed chest is not a Double Chest
                    if (isChestClaimed(blockAt, false)) return false;
                }
            }
        }
        return true;
    }

    public static int writeClaimToFile(Block targetBlock, Player commandExecutioner) {
        // Find out how many chests a player has already claimed
        int alreadyClaimedChests = alreadyClaimedChests(commandExecutioner.getPlayer().getUniqueId());
        if (alreadyClaimedChests >= 4) return 5;

        try {
            boolean doubleChest = false;
            String secondChest = "";

            // Handle double chests
            BlockState chestState = targetBlock.getState();
            Chest chest = (Chest) chestState;
            Inventory inventory = chest.getInventory();
            if (inventory instanceof DoubleChestInventory) {
                doubleChest = true;

                // Check where the second chest is
                DoubleChestInventory doubleChestInventory = (DoubleChestInventory) inventory;
                Chest leftChest = (Chest) doubleChestInventory.getLeftSide().getHolder();
                Chest rightChest = (Chest) doubleChestInventory.getRightSide().getHolder();
                Location leftChestLocation = leftChest.getLocation();
                Location rightChestLocation = rightChest.getLocation();

                // Find out which site is the clicked chest
                if (Objects.equals(targetBlock.getLocation().toString(), rightChestLocation.toString())) secondChest = leftChestLocation.toString();
                else if (Objects.equals(targetBlock.getLocation().toString(), leftChestLocation.toString())) secondChest = rightChestLocation.toString();
            }


            // Parse the XML file
            File xmlFile = new File("ClaimedChests.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            // Normalize the document
            doc.getDocumentElement().normalize();

            // Create new element
            Element chestElement = doc.createElement("claimedBlock");

            // Create sub-elements
            Element blockLocationElement = doc.createElement("blockLocation");
            Element playerOwnerElement = doc.createElement("playerOwner");
            Element numberOfChestsClaimed = doc.createElement("numberOfChestsClaimed");
            Element isDoubleChestElement = doc.createElement("isDoubleChest");
            Element doubleChestSideElement = doc.createElement("doubleChestSide");

            // Set the values of the sub-elements
            blockLocationElement.setTextContent(targetBlock.getLocation().toString());
            playerOwnerElement.setTextContent(Objects.requireNonNull(commandExecutioner.getPlayer()).getUniqueId().toString());
            numberOfChestsClaimed.setTextContent(String.valueOf(alreadyClaimedChests + 1));
            isDoubleChestElement.setTextContent(String.valueOf(doubleChest));
            doubleChestSideElement.setTextContent(secondChest);


            // Add the sub-elements to the chest element
            chestElement.appendChild(blockLocationElement);
            chestElement.appendChild(playerOwnerElement);
            chestElement.appendChild(isDoubleChestElement);
            chestElement.appendChild(doubleChestSideElement);

            // Add the element to the doc
            Element rootElement = doc.getDocumentElement();
            rootElement.appendChild(chestElement);

            // Write the document to a file
            doc.normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("ClaimedChests.xml"));
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return alreadyClaimedChests + 1;
    }

    private static int alreadyClaimedChests(UUID uniqueId) {
        int nrOfChests = 0;

        try {
            // Parse the XML file
            Document doc = getDocument();

            // Normalize the document
            doc.getDocumentElement().normalize();

            // Get the root element
            Element rootElement = doc.getDocumentElement();

            // Get all "claimedBlock" elements
            NodeList claimedBlockNodes = rootElement.getElementsByTagName("claimedBlock");

            for (int i = 0; i < claimedBlockNodes.getLength(); i++) {
                Element claimedBlockElement = (Element) claimedBlockNodes.item(i);

                Element playerOwnerElement = (Element) claimedBlockElement.getElementsByTagName("playerOwner").item(0);
                String playerOwner = playerOwnerElement.getTextContent();

                // Use the extracted data as needed
                if (Objects.equals(playerOwner, uniqueId.toString())) nrOfChests += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return nrOfChests;
    }

    public static boolean isChestClaimed(Block targetBlock, boolean shutdown) {
        try {
            // Parse the XML file
            Document doc = getDocument();

            // Normalize the document
            doc.getDocumentElement().normalize();

            // Get the root element
            Element rootElement = doc.getDocumentElement();

            // Get all of the "claimedBlock" elements
            NodeList claimedBlockNodes = rootElement.getElementsByTagName("claimedBlock");

            for (int i = 0; i < claimedBlockNodes.getLength(); i++) {
                Element claimedBlockElement = (Element) claimedBlockNodes.item(i);

                // Get the blockLocation element
                Element blockLocationElement = (Element) claimedBlockElement.getElementsByTagName("blockLocation").item(0);
                String blockLocation = blockLocationElement.getTextContent();

                // Get the isDoubleChest element
                Element isDoubleChestElement = (Element) claimedBlockElement.getElementsByTagName("isDoubleChest").item(0);
                String isDoubleChest = isDoubleChestElement.getTextContent();

                // Get the doubleChestSide element
                Element doubleChestSideElement = (Element) claimedBlockElement.getElementsByTagName("doubleChestSide").item(0);
                String doubleChestSide = doubleChestSideElement.getTextContent();

                // Use the extracted data as needed
                if (isDoubleChest.matches("true")) {
                    if (Objects.equals(blockLocation, targetBlock.getLocation().toString()) || Objects.equals(doubleChestSide, targetBlock.getLocation().toString())) return true;
                } else {
                    if (Objects.equals(blockLocation, targetBlock.getLocation().toString())) return true;
                }
            }
        } catch (FileNotFoundException e) {
            noFile();
            if (!shutdown) isChestClaimed(targetBlock,true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isChestClaimedByPlayer(Player player) {
        try {
            // Parse the XML file
            Document doc = getDocument();

            // Normalize the document
            doc.getDocumentElement().normalize();

            // Get the root element
            Element rootElement = doc.getDocumentElement();

            // Get all "claimedBlock" elements
            NodeList claimedBlockNodes = rootElement.getElementsByTagName("claimedBlock");

            for (int i = 0; i < claimedBlockNodes.getLength(); i++) {
                Element claimedBlockElement = (Element) claimedBlockNodes.item(i);

                Element playerOwnerElement = (Element) claimedBlockElement.getElementsByTagName("playerOwner").item(0);
                String playerOwner = playerOwnerElement.getTextContent();

                // Use the extracted data as needed
                if (Objects.equals(playerOwner, player.getPlayer().getUniqueId().toString())) return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void unclaimChest(Block targetBlock) {
        try {
            // Parse the doc
            Document doc = getDocument();

            // Normalize the document
            doc.getDocumentElement().normalize();

            // Find the "claimedBlock" element that you want to delete
            NodeList claimedBlockList = doc.getElementsByTagName("claimedBlock");
            for (int i = 0; i < claimedBlockList.getLength(); i++) {
                Node claimedBlockNode = claimedBlockList.item(i);
                if (claimedBlockNode.getNodeType() == Node.ELEMENT_NODE) {
                    String blockLocation;
                    Element claimedBlockElement = (Element) claimedBlockNode;

                    // Check if the chest is a Double Chest
                    String isDoubleChest = claimedBlockElement.getElementsByTagName("isDoubleChest").item(0).getTextContent();
                    if (isDoubleChest.matches("true")) blockLocation = claimedBlockElement.getElementsByTagName("doubleChestSide").item(0).getTextContent();
                    else blockLocation = claimedBlockElement.getElementsByTagName("blockLocation").item(0).getTextContent();

                    if (blockLocation.equals(targetBlock.getLocation().toString())) {
                        // This is the "claimedBlock" element that you want to delete
                        // Remove the element from the document
                        claimedBlockNode.getParentNode().removeChild(claimedBlockNode);
                    }
                }
            }

            // Write the updated document to the file
            doc.normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("ClaimedChests.xml"));
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
