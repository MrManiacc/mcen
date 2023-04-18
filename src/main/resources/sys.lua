function use(module, ...)
    for k,v in pairs(module) do
        if _G[k] then
            Console.warn("overriding symbol named '" .. k .. "'")
            _G[k] = module[k]
        else
            _G[k] = module[k]
        end
    end
end
