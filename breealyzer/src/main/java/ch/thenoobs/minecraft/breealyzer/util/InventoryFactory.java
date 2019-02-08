package ch.thenoobs.minecraft.breealyzer.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

public class InventoryFactory {
	private static HashMap<String, Class<? extends InventoryHandler>> typeMappings = new HashMap<String, Class<? extends InventoryHandler>>();
	private static HashMap<TileEntity, InventoryHandler> inventoryHandlers = new HashMap<TileEntity, InventoryHandler>();
	
	public static <T extends InventoryHandler> T GetInvenotryHandler(TileEntity tileEntity, IItemHandler itemHandler) {				
		T inventoryHandler = null;
		
		InventoryHandler cachedHandler = inventoryHandlers.get(tileEntity); 
		
		if (cachedHandler == null)
		{
			String typeName = tileEntity.getClass().getName();
			
			Class<? extends InventoryHandler> handlerType = typeMappings.get(typeName);		
						
			if (handlerType != null) {			
				try {
					Constructor<?> constructor = handlerType.getConstructor(tileEntity.getClass(), IItemHandler.class);
					
					inventoryHandler = (T)constructor.newInstance(tileEntity, itemHandler);
					
					inventoryHandlers.put(tileEntity, inventoryHandler);
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {

					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				inventoryHandler = (T) new InventoryHandler(tileEntity, itemHandler);
				
				inventoryHandlers.put(tileEntity, inventoryHandler);
			}
		}
		else {
			inventoryHandler = (T)cachedHandler;
		}
			
				
		return inventoryHandler;
	}
	
	public static Boolean registerInventoryHander(String typeName, Class<? extends InventoryHandler> handlerType) {
		Boolean isSuccessful = false;
		
		if ((typeName != null) && (!typeName.isEmpty())) {
			if (!typeMappings.containsKey(typeName)) {
				typeMappings.put(typeName, handlerType);
				isSuccessful = true;
			}
		}
		
		return isSuccessful;
	}
	
	public static List<Class<? extends InventoryHandler>> getRegisteredHandlers(){
		return typeMappings.values().stream().collect(Collectors.toList());
	}
}
